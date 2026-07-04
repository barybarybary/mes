package com.itheima.mes1.module.sale.service;

import cn.hutool.core.util.RandomUtil;
import java.time.format.DateTimeFormatter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.entity.SaleOrderItem;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SaleOrderService {

    @Autowired
    private SaleOrderMapper orderMapper;
    @Autowired
    private SaleOrderItemMapper itemMapper;

    public Page<SaleOrder> page(int page, int pageSize, Integer status, String keyword) {
        LambdaQueryWrapper<SaleOrder> w = new LambdaQueryWrapper<SaleOrder>()
                .eq(status != null, SaleOrder::getStatus, status)
                .orderByDesc(SaleOrder::getCreateTime);
        Page<SaleOrder> result = orderMapper.selectPage(new Page<>(page, pageSize), w);
        result.setTotal(orderMapper.selectCount(w));
        result.getRecords().forEach(o -> {
            SaleOrder full = orderMapper.selectWithCustomer(o.getId());
            if (full != null) o.setCustomerName(full.getCustomerName());
            o.setItems(itemMapper.selectByOrderId(o.getId()));
        });
        return result;
    }

    public SaleOrder getDetail(Long id) {
        SaleOrder order = orderMapper.selectWithCustomer(id);
        if (order != null) order.setItems(itemMapper.selectByOrderId(id));
        return order;
    }

    @Transactional
    public SaleOrder create(SaleOrder order) {
        order.setOrderNo("SO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + RandomUtil.randomNumbers(4));
        order.setStatus(1);
        orderMapper.insert(order);
        saveItems(order);
        return order;
    }

    @Transactional
    public void update(SaleOrder order) {
        SaleOrder exist = orderMapper.selectById(order.getId());
        if (exist.getStatus() != 1) throw new BusinessException("只有待审核的订单可以修改");
        orderMapper.updateById(order);
        if (order.getItems() != null) {
            itemMapper.deleteByOrderId(order.getId());
            saveItems(order);
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

    private void saveItems(SaleOrder order) {
        BigDecimal total = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (SaleOrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                item.setAmount(qty.multiply(price));
                itemMapper.insert(item);
                total = total.add(item.getAmount());
            }
        }
        order.setTotalAmount(total);
        orderMapper.updateById(order);
    }
}
