package com.itheima.mes1.module.sale.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.module.sale.entity.Delivery;
import com.itheima.mes1.module.sale.entity.DeliveryItem;
import com.itheima.mes1.module.sale.mapper.DeliveryItemMapper;
import com.itheima.mes1.module.sale.mapper.DeliveryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaleDeliveryService {

    @Autowired private DeliveryMapper deliveryMapper;
    @Autowired private DeliveryItemMapper deliveryItemMapper;

    public Page<Delivery> page(int page, int pageSize) {
        Page<Delivery> p = new Page<>(page, pageSize);
        deliveryMapper.selectPage(p, null);
        for (Delivery d : p.getRecords()) {
            Delivery detail = deliveryMapper.selectWithDetail(d.getId());
            if (detail != null) {
                d.setCustomerName(detail.getCustomerName());
                d.setOrderNo(detail.getOrderNo());
            }
            d.setItems(deliveryItemMapper.selectByDeliveryId(d.getId()));
        }
        return p;
    }

    public Delivery getDetail(Long id) {
        Delivery d = deliveryMapper.selectWithDetail(id);
        if (d != null) {
            d.setItems(deliveryItemMapper.selectByDeliveryId(id));
        }
        return d;
    }

    @Transactional
    public Delivery create(Delivery delivery) {
        deliveryMapper.insert(delivery);
        if (delivery.getItems() != null) {
            for (DeliveryItem item : delivery.getItems()) {
                item.setDeliveryId(delivery.getId());
                deliveryItemMapper.insert(item);
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