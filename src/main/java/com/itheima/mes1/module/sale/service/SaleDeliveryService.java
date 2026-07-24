package com.itheima.mes1.module.sale.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.module.inventory.service.InventoryService;
import com.itheima.mes1.module.sale.SaleConverter;
import com.itheima.mes1.module.sale.entity.Delivery;
import com.itheima.mes1.module.sale.entity.DeliveryItem;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.entity.SaleOrderItem;
import com.itheima.mes1.module.sale.mapper.*;
import com.itheima.mes1.module.sale.vo.DeliveryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SaleDeliveryService {

    @Autowired private DeliveryMapper deliveryMapper;
    @Autowired private DeliveryItemMapper deliveryItemMapper;
    @Autowired private SaleOrderMapper saleOrderMapper;
    @Autowired private SaleOrderItemMapper saleOrderItemMapper;
    @Autowired private InventoryService inventoryService;

    public Page<DeliveryVO> page(int page, int pageSize) {
        Page<Delivery> p = new Page<>(page, pageSize);
        deliveryMapper.selectPage(p, null);
        Page<DeliveryVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(d -> {
            Delivery detail = deliveryMapper.selectWithDetail(d.getId());
            DeliveryVO vo = SaleConverter.toVO(detail != null ? detail : d);
            vo.setItems(SaleConverter.toDeliveryItemVOList(deliveryItemMapper.selectByDeliveryId(d.getId())));
            return vo;
        }).toList());
        return voPage;
    }

    public DeliveryVO getDetail(Long id) {
        Delivery d = deliveryMapper.selectWithDetail(id);
        if (d == null) return null;
        DeliveryVO vo = SaleConverter.toVO(d);
        vo.setItems(SaleConverter.toDeliveryItemVOList(deliveryItemMapper.selectByDeliveryId(id)));
        return vo;
    }

    @Transactional
    public DeliveryVO create(Delivery delivery) {
        deliveryMapper.insert(delivery);
        if (delivery.getItems() != null) {
            for (DeliveryItem item : delivery.getItems()) {
                item.setDeliveryId(delivery.getId());
                deliveryItemMapper.insert(item);

                // 扣减库存
                inventoryService.stockOut(item.getProductId(), null, null,
                        item.getQuantity(), "销售发货", delivery.getDeliveryNo(), null);

                // 更新订单明细的已发货数量
                if (delivery.getOrderId() != null) {
                    SaleOrderItem soi = saleOrderItemMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SaleOrderItem>()
                                    .eq(SaleOrderItem::getOrderId, delivery.getOrderId())
                                    .eq(SaleOrderItem::getProductId, item.getProductId()));
                    if (soi != null) {
                        BigDecimal before = soi.getDeliveredQty() != null ? soi.getDeliveredQty() : BigDecimal.ZERO;
                        soi.setDeliveredQty(before.add(item.getQuantity()));
                        saleOrderItemMapper.updateById(soi);
                    }
                }
            }
        }

        // 检查订单是否全部发货完成 → 更新订单状态
        if (delivery.getOrderId() != null) {
            java.util.List<SaleOrderItem> orderItems = saleOrderItemMapper.selectByOrderId(delivery.getOrderId());
            boolean allDelivered = orderItems.stream().allMatch(i ->
                    i.getDeliveredQty() != null && i.getDeliveredQty().compareTo(i.getQuantity()) >= 0);
            if (allDelivered) {
                SaleOrder order = saleOrderMapper.selectById(delivery.getOrderId());
                if (order != null) {
                    order.setStatus(5); // 已完成
                    saleOrderMapper.updateById(order);
                }
            } else {
                SaleOrder order = saleOrderMapper.selectById(delivery.getOrderId());
                if (order != null && order.getStatus() < 4) {
                    order.setStatus(4); // 部分发货
                    saleOrderMapper.updateById(order);
                }
            }
        }

        return getDetail(delivery.getId());
    }

    @Transactional
    public void update(Delivery delivery) {
        deliveryMapper.updateById(delivery);
        if (delivery.getItems() != null) {
            deliveryItemMapper.deleteByDeliveryId(delivery.getId());
            for (DeliveryItem item : delivery.getItems()) {
                item.setDeliveryId(delivery.getId());
                deliveryItemMapper.insert(item);
            }
        }
    }

    public void updateStatus(Long id, Integer status) {
        Delivery d = new Delivery();
        d.setId(id);
        d.setStatus(status);
        deliveryMapper.updateById(d);
    }

    @Transactional
    public void delete(Long id) {
        deliveryItemMapper.deleteByDeliveryId(id);
        deliveryMapper.deleteById(id);
    }
}
