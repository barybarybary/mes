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

import com.itheima.mes1.module.inventory.service.InventoryService;
import com.itheima.mes1.module.sale.entity.Delivery;
import com.itheima.mes1.module.sale.mapper.DeliveryMapper;
import com.itheima.mes1.module.dashboard.entity.OrderNotification;
import com.itheima.mes1.module.dashboard.mapper.OrderNotificationMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Autowired private InventoryService inventoryService;
    @Autowired private DeliveryMapper deliveryMapper;
    @Autowired private OrderNotificationMapper orderNotificationMapper;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            1, "待付款", 2, "已支付", 3, "生产中", 4, "已发货", 5, "已完成", 6, "已取消"
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
        // 收集 categoryId 及其所有子分类
        Set<Long> categoryIds = new HashSet<>();
        if (categoryId != null) {
            categoryIds.add(categoryId);
            collectChildIds(categoryId, categoryIds);
        }

        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .in(!categoryIds.isEmpty(), Product::getCategoryId, categoryIds)
                .eq(categoryId != null && categoryIds.isEmpty(), Product::getCategoryId, categoryId)
                .and(cn.hutool.core.util.StrUtil.isNotBlank(keyword),
                        q -> q.like(Product::getName, keyword).or().like(Product::getCode, keyword))
                .orderByDesc(Product::getCreateTime);

        Page<Product> result = productMapper.selectPage(new Page<>(page, pageSize), w);
        // 预加载所有分类，建立 id→name 映射
        Map<Long, String> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
        // 批量查询库存
        List<Long> productIds = result.getRecords().stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, Integer> stockMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<Map<String, Object>> stocks = inventoryMapper.sumQuantityByProductIds(productIds);
            for (Map<String, Object> row : stocks) {
                Long pid = ((Number) row.get("product_id")).longValue();
                Integer qty = ((Number) row.get("stock")).intValue();
                stockMap.put(pid, qty);
            }
        }
        Page<ProductCatalogVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(p -> {
            ProductCatalogVO vo = new ProductCatalogVO();
            BeanUtils.copyProperties(p, vo);
            vo.setCategoryName(categoryMap.get(p.getCategoryId()));
            vo.setStockQuantity(stockMap.getOrDefault(p.getId(), 0));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    /** 递归收集所有子分类ID */
    private void collectChildIds(Long parentId, Set<Long> result) {
        List<ProductCategory> children = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getParentId, parentId));
        for (ProductCategory c : children) {
            result.add(c.getId());
            collectChildIds(c.getId(), result);
        }
    }

    /** 返回分类树（支持多级） */
    public List<Map<String, Object>> listCategories() {
        List<ProductCategory> all = categoryMapper.selectList(null);
        // id → node 映射
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (ProductCategory c : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", c.getId());
            node.put("name", c.getName());
            node.put("parentId", c.getParentId());
            node.put("sort", c.getSort());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(c.getId(), node);
        }
        for (ProductCategory c : all) {
            Map<String, Object> node = nodeMap.get(c.getId());
            if (c.getParentId() == null || c.getParentId() == 0) {
                roots.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(c.getParentId());
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(node);
                } else {
                    roots.add(node); // 父分类已删除时挂到根
                }
            }
        }
        return roots;
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

        PortalCustomer customer = customerMapper.selectById(customerId);
        if (customer == null) throw new BusinessException("客户不存在");

        SaleOrder order = new SaleOrder();
        order.setOrderNo("SO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + RandomUtil.randomNumbers(4));
        order.setCustomerId(customerId);
        order.setCustomerName(customer != null ? customer.getCompanyName() : "客户#" + customerId);
        order.setOrderDate(LocalDate.now());
        order.setDeliveryDate(req.getDeliveryDate());
        order.setStatus(1);
        order.setPaid(0); // 初始未付款
        order.setReceiverName(req.getReceiverName());
        order.setReceiverPhone(req.getReceiverPhone());
        order.setReceiverAddress(req.getReceiverAddress());
        order.setRemark(req.getRemark() != null && !req.getRemark().isBlank() ? req.getRemark() : null);
        order.setCreateBy(customerId);
        saleOrderMapper.insert(order);

        BigDecimal total = BigDecimal.ZERO;
        for (PlaceOrderReq.OrderItemReq item : req.getItems()) {
            Product p = productMapper.selectById(item.getProductId());
            if (p == null) throw new BusinessException("产品不存在: " + item.getProductId());

            // 扣减库存
            try {
                inventoryService.stockOut(item.getProductId(), null, null,
                        BigDecimal.valueOf(item.getQuantity()), "销售出库", order.getOrderNo(), null);
            } catch (BusinessException e) {
                throw new BusinessException("产品「" + p.getName() + "」库存不足，请调整数量");
            }

            SaleOrderItem soi = new SaleOrderItem();
            soi.setOrderId(order.getId());
            soi.setProductId(item.getProductId());
            soi.setQuantity(BigDecimal.valueOf(item.getQuantity()));
            soi.setUnit(p.getUnit());
            BigDecimal price;
            if (p.getPrice() != null && p.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                price = p.getPrice();
            } else if (item.getPrice() != null && item.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                price = item.getPrice();
            } else {
                price = BigDecimal.ZERO;
            }
            soi.setPrice(price);
            soi.setAmount(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            total = total.add(soi.getAmount());
            saleOrderItemMapper.insert(soi);
        }
        order.setTotalAmount(total);
        saleOrderMapper.updateById(order);

        // 清空购物车
        clearCart(customerId);

        // 写入订单通知（下单即通知管理员，不等到支付）
        try {
            OrderNotification notification = new OrderNotification();
            notification.setOrderId(order.getId());
            notification.setOrderNo(order.getOrderNo());
            notification.setCustomerName(customer != null ? customer.getCompanyName() : "客户#" + customerId);
            notification.setTotalAmount(total);
            notification.setIsRead(0);
            orderNotificationMapper.insert(notification);
        } catch (Exception ignored) {
            // order_notification 表可能不存在，容错
        }

        return getOrderDetail(customerId, order.getId());
    }

    // ==================== 订单查询 ====================

    public Page<PortalOrderVO> listOrders(Long customerId, int page, int pageSize, Integer status) {
        LambdaQueryWrapper<SaleOrder> w = new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getCustomerId, customerId)
                .eq(status != null, SaleOrder::getStatus, status)
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

    /** 模拟支付 */
    @Transactional
    public void payOrder(Long customerId, Long orderId) {
        SaleOrder order = saleOrderMapper.selectById(orderId);
        if (order == null || !order.getCustomerId().equals(customerId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getPaid() != null && order.getPaid() == 1) {
            throw new BusinessException("订单已支付");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("当前订单状态不可支付");
        }
        order.setPaid(1);
        order.setStatus(2); // 已支付
        saleOrderMapper.updateById(order);

        // 写入订单支付通知，后台管理员可见
        try {
            PortalCustomer customer = customerMapper.selectById(customerId);
            OrderNotification notification = new OrderNotification();
            notification.setOrderId(order.getId());
            notification.setOrderNo(order.getOrderNo());
            notification.setCustomerName(customer != null ? customer.getCompanyName() : "客户#" + customerId);
            notification.setTotalAmount(order.getTotalAmount());
            notification.setIsRead(0);
            orderNotificationMapper.insert(notification);
        } catch (Exception ignored) {
            // order_notification 表可能不存在，容错
        }
    }

    /** 取消订单（仅 status=1 待付款时可取消） */
    @Transactional
    public void cancelOrder(Long customerId, Long orderId) {
        SaleOrder order = saleOrderMapper.selectById(orderId);
        if (order == null || !order.getCustomerId().equals(customerId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("仅待付款状态的订单可以取消");
        }
        order.setStatus(6); // 已取消
        saleOrderMapper.updateById(order);

        // 退还库存
        List<SaleOrderItem> items = saleOrderItemMapper.selectByOrderId(orderId);
        for (SaleOrderItem item : items) {
            inventoryService.stockIn(item.getProductId(), null, null, null,
                    item.getQuantity(), "订单取消退还", order.getOrderNo(), null);
        }
    }

    /** 根据订单状态构建进度时间线 */
    private List<OrderProgressItemVO> buildProgressTimeline(SaleOrder order, Delivery latestDelivery) {
        List<OrderProgressItemVO> timeline = new ArrayList<>();
        Integer status = order.getStatus();

        // 已取消 — 只显示到取消前的步骤 + 已取消
        if (status != null && status == 6) {
            timeline.add(buildItem("已下单", "订单已提交，等待付款", order.getCreateTime(), "completed", "clock"));
            timeline.add(buildItem("已取消", "订单已取消，款项将退回", order.getUpdateTime(), "completed", "close"));
            // 如果有发货也显示
            if (latestDelivery != null && latestDelivery.getDeliveryNo() != null) {
                timeline.add(2, buildItem("部分发货", "部分商品已发出: " + latestDelivery.getDeliveryNo(),
                        latestDelivery.getDeliveryDate() != null ? latestDelivery.getDeliveryDate().atStartOfDay() : latestDelivery.getCreateTime(),
                        "completed", "truck"));
            }
            return timeline;
        }

        // === 步骤1: 已下单 ===
        timeline.add(buildItem("已下单", "订单已提交，等待付款",
                order.getCreateTime(), "completed", "clock"));

        // === 步骤2: 已付款 ===
        boolean paid = order.getPaid() != null && order.getPaid() == 1;
        String payStatus = paid || (status != null && status >= 2) ? "completed" : (status != null && status == 1 ? "active" : "pending");
        String payDesc;
        if ("completed".equals(payStatus)) {
            payDesc = "付款成功，订单进入审核流程";
        } else if ("active".equals(payStatus)) {
            payDesc = "等待付款";
        } else {
            payDesc = "待付款后进入审核";
        }
        timeline.add(buildItem("已付款", payDesc,
                ("completed".equals(payStatus) || "active".equals(payStatus)) && paid ? order.getUpdateTime() : null,
                payStatus, "wallet"));

        // === 步骤3: 生产中 ===
        String prodStatus;
        String prodDesc;
        if (status != null && status >= 4) {
            prodStatus = "completed";
            prodDesc = "生产已完成，进入发货环节";
        } else if (status != null && status == 3) {
            prodStatus = "active";
            prodDesc = "正在生产中，请耐心等待";
        } else {
            prodStatus = "pending";
            prodDesc = "付款后将安排生产";
        }
        timeline.add(buildItem("生产中", prodDesc, null, prodStatus, "setting"));

        // === 步骤4: 已发货 ===
        String shipStatus;
        String shipDesc;
        LocalDateTime shipTime = null;
        if (status != null && status >= 5) {
            shipStatus = "completed";
            shipDesc = "货物已送达";
            shipTime = order.getUpdateTime();
        } else if (status != null && status == 4) {
            shipStatus = "active";
            shipDesc = "货物运输途中";
            if (latestDelivery != null) {
                shipTime = latestDelivery.getDeliveryDate() != null
                        ? latestDelivery.getDeliveryDate().atStartOfDay()
                        : latestDelivery.getCreateTime();
                shipDesc = latestDelivery.getDeliveryNo() != null
                        ? "运输途中，发货单号: " + latestDelivery.getDeliveryNo()
                        : "运输途中";
            }
        } else {
            shipStatus = "pending";
            shipDesc = "等待发货";
        }
        timeline.add(buildItem("已发货", shipDesc, shipTime, shipStatus, "truck"));

        // === 步骤5: 已完成 ===
        String doneStatus;
        String doneDesc;
        if (status != null && status == 5) {
            doneStatus = "completed";
            doneDesc = "订单已完成，感谢您的购买";
        } else {
            doneStatus = "pending";
            doneDesc = "待订单完成后确认收货";
        }
        timeline.add(buildItem("已完成", doneDesc,
                "completed".equals(doneStatus) ? order.getUpdateTime() : null,
                doneStatus, "check"));

        return timeline;
    }

    private OrderProgressItemVO buildItem(String title, String description, LocalDateTime time, String status, String icon) {
        OrderProgressItemVO item = new OrderProgressItemVO();
        item.setTitle(title);
        item.setDescription(description);
        item.setTime(time);
        item.setStatus(status);
        item.setIcon(icon);
        return item;
    }

    private PortalOrderVO buildOrderVO(SaleOrder order) {
        PortalOrderVO vo = new PortalOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderDate(order.getOrderDate());
        vo.setStatus(order.getStatus());
        vo.setStatusText(STATUS_MAP.getOrDefault(order.getStatus(), "未知"));
        vo.setPaid(order.getPaid() != null ? order.getPaid() : 0);
        vo.setTotalAmount(order.getTotalAmount());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        // 填充客户收货信息
        PortalCustomer customer = customerMapper.selectById(order.getCustomerId());
        if (customer != null) {
            vo.setCompanyName(customer.getCompanyName());
            vo.setContactName(customer.getContactName());
            vo.setPhone(customer.getPhone());
            vo.setAddress(customer.getAddress());
        }

        // 关联发货信息：找该订单最新的发货单
        Delivery latestDelivery = null;
        List<Delivery> deliveries = deliveryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Delivery>()
                        .eq(Delivery::getOrderId, order.getId())
                        .orderByDesc(Delivery::getCreateTime));
        if (deliveries != null && !deliveries.isEmpty()) {
            latestDelivery = deliveries.get(0);
            vo.setDeliveryNo(latestDelivery.getDeliveryNo());
            vo.setDeliveryDate(latestDelivery.getDeliveryDate());
        }

        // 订单进度时间线
        vo.setProgressTimeline(buildProgressTimeline(order, latestDelivery));

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
                ivo.setImageUrl(p.getImageUrl());
            }
            itemVOs.add(ivo);
        }
        vo.setItems(itemVOs);
        return vo;
    }

    // ==================== 个人中心 ====================

    public Map<String, Object> getProfileStats(Long customerId) {
        Map<String, Object> stats = new HashMap<>();
        // 各状态订单数量
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getCustomerId, customerId));
        long pendingPay = orders.stream().filter(o -> o.getStatus() == 1).count();
        long paid = orders.stream().filter(o -> o.getStatus() == 2).count();
        long inProduction = orders.stream().filter(o -> o.getStatus() == 3).count();
        long shipped = orders.stream().filter(o -> o.getStatus() == 4 || o.getStatus() == 5).count();
        stats.put("totalOrders", orders.size());
        stats.put("pendingPay", pendingPay);
        stats.put("paid", paid);
        stats.put("inProduction", inProduction);
        stats.put("shipped", shipped);
        return stats;
    }

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
