package com.itheima.mes1.module.sale;

import com.itheima.mes1.module.sale.dto.*;
import com.itheima.mes1.module.sale.entity.*;
import com.itheima.mes1.module.sale.vo.*;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sale 模块 Entity ↔ DTO/VO 转换工具
 */
public class SaleConverter {

    // ==================== SaleOrder ====================

    public static SaleOrder toEntity(SaleOrderCreateReq req) {
        SaleOrder order = new SaleOrder();
        BeanUtils.copyProperties(req, order, "items");
        if (req.getItems() != null) {
            // items handled in service
        }
        return order;
    }

    public static SaleOrder toEntity(SaleOrderUpdateReq req) {
        SaleOrder order = new SaleOrder();
        BeanUtils.copyProperties(req, order, "items");
        return order;
    }

    public static SaleOrderItem toEntity(SaleOrderItemReq req) {
        SaleOrderItem item = new SaleOrderItem();
        BeanUtils.copyProperties(req, item);
        return item;
    }

    public static SaleOrderVO toVO(SaleOrder order) {
        if (order == null) return null;
        SaleOrderVO vo = new SaleOrderVO();
        BeanUtils.copyProperties(order, vo, "items");
        return vo;
    }

    public static SaleOrderItemVO toVO(SaleOrderItem item) {
        if (item == null) return null;
        SaleOrderItemVO vo = new SaleOrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    public static List<SaleOrderItemVO> toItemVOList(List<SaleOrderItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream().map(SaleConverter::toVO).collect(Collectors.toList());
    }

    // ==================== Delivery ====================

    public static DeliveryVO toVO(Delivery delivery) {
        if (delivery == null) return null;
        DeliveryVO vo = new DeliveryVO();
        BeanUtils.copyProperties(delivery, vo, "items");
        return vo;
    }

    public static DeliveryItemVO toVO(DeliveryItem item) {
        if (item == null) return null;
        DeliveryItemVO vo = new DeliveryItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    public static List<DeliveryItemVO> toDeliveryItemVOList(List<DeliveryItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream().map(SaleConverter::toVO).collect(Collectors.toList());
    }
}
