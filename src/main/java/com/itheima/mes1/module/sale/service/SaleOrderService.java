package com.itheima.mes1.module.sale.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.module.sale.SaleConverter;
import com.itheima.mes1.module.sale.dto.SaleOrderCreateReq;
import com.itheima.mes1.module.sale.dto.SaleOrderItemReq;
import com.itheima.mes1.module.sale.dto.SaleOrderUpdateReq;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.entity.SaleOrderItem;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import com.itheima.mes1.module.sale.vo.SaleOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SaleOrderService {

    @Autowired
    private SaleOrderMapper orderMapper;
    @Autowired
    private SaleOrderItemMapper itemMapper;
    @Autowired
    private com.itheima.mes1.module.base.mapper.CustomerMapper customerMapper;

    public Page<SaleOrderVO> page(int page, int pageSize, Integer status, String keyword) {
        LambdaQueryWrapper<SaleOrder> w = new LambdaQueryWrapper<SaleOrder>()
                .eq(status != null, SaleOrder::getStatus, status)
                .orderByDesc(SaleOrder::getCreateTime);
        Page<SaleOrder> result = orderMapper.selectPage(new Page<>(page, pageSize), w);
        result.setTotal(orderMapper.selectCount(w));

        Page<SaleOrderVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(o -> {
            SaleOrder full = orderMapper.selectWithCustomer(o.getId());
            SaleOrderVO vo = SaleConverter.toVO(full != null ? full : o);
            vo.setItems(SaleConverter.toItemVOList(itemMapper.selectByOrderId(o.getId())));
            return vo;
        }).toList());
        return voPage;
    }

    public SaleOrderVO getDetail(Long id) {
        SaleOrder order = orderMapper.selectWithCustomer(id);
        if (order == null) return null;
        SaleOrderVO vo = SaleConverter.toVO(order);
        vo.setItems(SaleConverter.toItemVOList(itemMapper.selectByOrderId(id)));
        return vo;
    }

    @Transactional
    public SaleOrderVO create(SaleOrderCreateReq req) {
        SaleOrder order = new SaleOrder();
        order.setCustomerId(req.getCustomerId());
        var customer = customerMapper.selectById(req.getCustomerId());
        order.setCustomerName(customer != null ? customer.getName() : null);
        order.setOrderDate(req.getOrderDate());
        order.setDeliveryDate(req.getDeliveryDate());
        order.setRemark(req.getRemark());
        order.setReceiverName(req.getReceiverName());
        order.setReceiverPhone(req.getReceiverPhone());
        order.setReceiverAddress(req.getReceiverAddress());
        order.setOrderNo("SO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + RandomUtil.randomNumbers(4));
        order.setStatus(1);
        orderMapper.insert(order);

        BigDecimal total = saveItemsFromReq(order.getId(), req.getItems());
        order.setTotalAmount(total);
        orderMapper.updateById(order);

        return getDetail(order.getId());
    }

    @Transactional
    public void update(SaleOrderUpdateReq req) {
        SaleOrder exist = orderMapper.selectById(req.getId());
        if (exist.getStatus() != 1) throw new BusinessException("只有待审核的订单可以修改");

        SaleOrder order = new SaleOrder();
        order.setId(req.getId());
        order.setDeliveryDate(req.getDeliveryDate());
        order.setRemark(req.getRemark());
        orderMapper.updateById(order);

        if (req.getItems() != null) {
            itemMapper.deleteByOrderId(req.getId());
            BigDecimal total = saveItemsFromReq(req.getId(), req.getItems());
            order.setTotalAmount(total);
            orderMapper.updateById(order);
        }
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        orderMapper.updateStatus(id, status);
    }

    @Transactional
    public void delete(Long id) {
        itemMapper.deleteByOrderId(id);
        orderMapper.deleteById(id);
    }

    private BigDecimal saveItemsFromReq(Long orderId, List<SaleOrderItemReq> itemReqs) {
        BigDecimal total = BigDecimal.ZERO;
        if (itemReqs != null) {
            for (SaleOrderItemReq req : itemReqs) {
                SaleOrderItem item = SaleConverter.toEntity(req);
                item.setOrderId(orderId);
                BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                item.setAmount(qty.multiply(price));
                itemMapper.insert(item);
                total = total.add(item.getAmount());
            }
        }
        return total;
    }
}
