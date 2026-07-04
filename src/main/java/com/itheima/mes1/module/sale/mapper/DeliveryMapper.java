package com.itheima.mes1.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.sale.entity.Delivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeliveryMapper extends BaseMapper<Delivery> {

    @Select("SELECT d.*, c.name as customer_name, so.order_no " +
            "FROM delivery d " +
            "LEFT JOIN customer c ON d.customer_id = c.id " +
            "LEFT JOIN sale_order so ON d.order_id = so.id " +
            "WHERE d.id = #{id}")
    Delivery selectWithDetail(@Param("id") Long id);

    @Select("SELECT d.*, c.name as customer_name, so.order_no " +
            "FROM delivery d " +
            "LEFT JOIN customer c ON d.customer_id = c.id " +
            "LEFT JOIN sale_order so ON d.order_id = so.id " +
            "ORDER BY d.create_time DESC")
    List<Delivery> selectAllWithDetail();
}
