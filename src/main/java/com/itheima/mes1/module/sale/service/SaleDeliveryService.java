package com.itheima.mes1.module.sale.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.module.sale.SaleConverter;
import com.itheima.mes1.module.sale.entity.Delivery;
import com.itheima.mes1.module.sale.entity.DeliveryItem;
import com.itheima.mes1.module.sale.mapper.DeliveryItemMapper;
import com.itheima.mes1.module.sale.mapper.DeliveryMapper;
import com.itheima.mes1.module.sale.vo.DeliveryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleDeliveryService {

    @Autowired private DeliveryMapper deliveryMapper;
    @Autowired private DeliveryItemMapper deliveryItemMapper;

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
