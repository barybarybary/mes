package com.itheima.mes1.module.portal.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.module.base.entity.Product;
import com.itheima.mes1.module.base.entity.ProductCategory;
import com.itheima.mes1.module.base.mapper.ProductCategoryMapper;
import com.itheima.mes1.module.base.mapper.ProductMapper;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import com.itheima.mes1.module.portal.dto.PlaceOrderReq;
import com.itheima.mes1.module.portal.dto.PortalLoginReq;
import com.itheima.mes1.module.portal.dto.PortalRegisterReq;
import com.itheima.mes1.module.portal.entity.PortalCustomer;
import com.itheima.mes1.module.portal.mapper.PortalCustomerMapper;
import com.itheima.mes1.module.portal.vo.*;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.entity.SaleOrderItem;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PortalService {

    @Autowired private PortalCustomerMapper customerMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private SaleOrderMapper saleOrderMapper;
    @Autowired private SaleOrderItemMapper saleOrderItemMapper;
    @Autowired private InventoryMapper inventoryMapper;
    @Autowired private ProductCategoryMapper categoryMapper;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            1, "待审核", 2, "已审核", 3, "生产中", 4, "部分发货", 5, "已完成", 6, "已取消"
    );

    // ==================== 注册/登录 ====================

    @Transactional
    public PortalLoginVO register(PortalRegisterReq req) {
        if (customerMapper.selectCount(new LambdaQueryWrapper<PortalCustomer>()
                .eq(PortalCustomer::getUsername, req.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        PortalCustomer customer = new PortalCustomer();
        BeanUtils.copyProperties(req, customer);
        customer.setPassword(passwordEncoder.encode(req.getPassword()));
        customer.setStatus(1);
        customerMapper.insert(customer);
        return buildLoginVO(customer);
    }

    public PortalLoginVO login(PortalLoginReq req) {
        PortalCustomer customer = customerMapper.selectOne(new LambdaQueryWrapper<PortalCustomer>()
                .eq(PortalCustomer::getUsername, req.getUsername()));
        if (customer == null || !passwordEncoder.matches(req.getPassword(), customer.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (customer.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        return buildLoginVO(customer);
    }

    private PortalLoginVO buildLoginVO(PortalCustomer customer) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("portal_token:" + token, customer.getId(), 24, TimeUnit.HOURS);

        PortalLoginVO vo = new PortalLoginVO();
        vo.setToken(token);
        vo.setCustomer(toCustomerVO(customer));
        return vo;
    }

    /** 从 token 获取客户 ID */
    public Long getCustomerId(String token) {
        Object id = redisTemplate.opsForValue().get("portal_token:" + token);
        if (id == null) throw new BusinessException("登录已过期，请重新登录");
        return ((Number) id).longValue();
    }

    // ==================== 产品浏览 ====================

    public Page<ProductCatalogVO> listProducts(int page, int pageSize, String keyword, Long categoryId) {
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .and(cn.hutool.core.util.StrUtil.isNotBlank(keyword),
                        q -> q.like(Product::getName, keyword).or().like(Product::getCode, keyword))
                .orderByDesc(Product::getCreateTime);

        Page<Product> result = productMapper.selectPage(new Page<>(page, pageSize), w);
        // 预加载所有分类，建立 id→name 映射
        Map<Long, String> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
        Page<ProductCatalogVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(p -> {
            ProductCatalogVO vo = new ProductCatalogVO();
            BeanUtils.copyProperties(p, vo);
            vo.setCategoryName(categoryMap.get(p.getCategoryId()));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    public List<Map<String, Object>> listCategories() {
        return categoryMapper.selectList(null).stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName()))
                .collect(Collectors.toList());
    }

    public ProductDetailVO getProductDetail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null || p.getStatus() == 0) throw new BusinessException("产品不存在或已下架");
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(p, vo);
        // 查询库存总量
        Integer stock = inventoryMapper.sumQuantityByProduct(id);
        vo.setStockQuantity(stock != null ? stock : 0);
        return vo;
    }

    // ==================== 购物车（Redis） ====================

    public void addToCart(Long customerId, Long productId, int quantity) {
        String key = "portal_cart:" + customerId;
        redisTemplate.opsForHash().put(key, productId.toString(), quantity);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCart(Long customerId) {
        String key = "portal_cart:" + customerId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            Long productId = Long.valueOf(e.getKey().toString());
            int qty = Integer.parseInt(e.getValue().toString());
            Product p = productMapper.selectById(productId);
            if (p != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("productId", productId);
                item.put("productName", p.getName());
                item.put("productCode", p.getCode());
                item.put("spec", p.getSpec());
                item.put("price", p.getPrice());
                item.put("quantity", qty);
                item.put("imageUrl", p.getImageUrl());
                list.add(item);
            }
        }
        return list;
    }

    public void updateCartItem(Long customerId, Long productId, int quantity) {
        String key = "portal_cart:" + customerId;
        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(key, productId.toString());
        } else {
            redisTemplate.opsForHash().put(key, productId.toString(), quantity);
        }
    }

    public void clearCart(Long customerId) {
        redisTemplate.delete("portal_cart:" + customerId);
    }

    // ==================== 下单 ====================

    @Transactional
    public PortalOrderVO placeOrder(Long customerId, PlaceOrderReq req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BusinessException("请至少选择一个产品");
        }

        SaleOrder order = new SaleOrder();
        order.setOrderNo("SO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + RandomUtil.randomNumbers(4));
        order.setOrderDate(LocalDate.now());
        order.setStatus(1);
        order.setRemark(req.getRemark());
        order.setCreateBy(customerId);
        saleOrderMapper.insert(order);

        BigDecimal total = BigDecimal.ZERO;
        for (PlaceOrderReq.OrderItemReq item : req.getItems()) {
            Product p = productMapper.selectById(item.getProductId());
            if (p == null) throw new BusinessException("产品不存在: " + item.getProductId());

            SaleOrderItem soi = new SaleOrderItem();
            soi.setOrderId(order.getId());
            soi.setProductId(item.getProductId());
            soi.setQuantity(BigDecimal.valueOf(item.getQuantity()));
            soi.setUnit(p.getUnit());
            soi.setPrice(p.getPrice());
            soi.setAmount(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            total = total.add(soi.getAmount());
            saleOrderItemMapper.insert(soi);
        }
        order.setTotalAmount(total);
        saleOrderMapper.insert(order);

        // 清空购物车
        clearCart(customerId);

        return getOrderDetail(customerId, order.getId());
    }

    // ==================== 订单查询 ====================

    public Page<PortalOrderVO> listOrders(Long customerId, int page, int pageSize) {
        LambdaQueryWrapper<SaleOrder> w = new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getCustomerId, customerId)
                .orderByDesc(SaleOrder::getCreateTime);
        Page<SaleOrder> result = saleOrderMapper.selectPage(new Page<>(page, pageSize), w);

        Page<PortalOrderVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(o -> buildOrderVO(o))
                .collect(Collectors.toList()));
        return voPage;
    }

    public PortalOrderVO getOrderDetail(Long customerId, Long orderId) {
        SaleOrder order = saleOrderMapper.selectById(orderId);
        if (order == null || !order.getCustomerId().equals(customerId)) {
            throw new BusinessException("订单不存在");
        }
        return buildOrderVO(order);
    }

    private PortalOrderVO buildOrderVO(SaleOrder order) {
        PortalOrderVO vo = new PortalOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderDate(order.getOrderDate());
        vo.setStatus(order.getStatus());
        vo.setStatusText(STATUS_MAP.getOrDefault(order.getStatus(), "未知"));
        vo.setTotalAmount(order.getTotalAmount());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        List<SaleOrderItem> items = saleOrderItemMapper.selectByOrderId(order.getId());
        List<PortalOrderVO.PortalOrderItemVO> itemVOs = new ArrayList<>();
        for (SaleOrderItem item : items) {
            PortalOrderVO.PortalOrderItemVO ivo = new PortalOrderVO.PortalOrderItemVO();
            ivo.setProductId(item.getProductId());
            ivo.setQuantity(item.getQuantity());
            ivo.setUnit(item.getUnit());
            ivo.setPrice(item.getPrice());
            ivo.setAmount(item.getAmount());
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) {
                ivo.setProductName(p.getName());
                ivo.setProductCode(p.getCode());
            }
            itemVOs.add(ivo);
        }
        vo.setItems(itemVOs);
        return vo;
    }

    // ==================== 个人中心 ====================

    public CustomerVO getProfile(Long customerId) {
        PortalCustomer c = customerMapper.selectById(customerId);
        return toCustomerVO(c);
    }

    public CustomerVO updateProfile(Long customerId, Map<String, String> body) {
        PortalCustomer c = customerMapper.selectById(customerId);
        if (body.containsKey("companyName")) c.setCompanyName(body.get("companyName"));
        if (body.containsKey("contactName")) c.setContactName(body.get("contactName"));
        if (body.containsKey("phone")) c.setPhone(body.get("phone"));
        if (body.containsKey("email")) c.setEmail(body.get("email"));
        if (body.containsKey("address")) c.setAddress(body.get("address"));
        customerMapper.updateById(c);
        return toCustomerVO(c);
    }

    public void changePassword(Long customerId, String oldPwd, String newPwd) {
        PortalCustomer c = customerMapper.selectById(customerId);
        if (!passwordEncoder.matches(oldPwd, c.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        c.setPassword(passwordEncoder.encode(newPwd));
        customerMapper.updateById(c);
    }

    // ==================== 转换 ====================

    private CustomerVO toCustomerVO(PortalCustomer c) {
        CustomerVO vo = new CustomerVO();
        BeanUtils.copyProperties(c, vo);
        return vo;
    }
}
