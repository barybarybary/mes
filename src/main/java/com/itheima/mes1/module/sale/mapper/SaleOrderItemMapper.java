package com.itheima.mes1.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.sale.entity.SaleOrderItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SaleOrderItemMapper extends BaseMapper<SaleOrderItem> {
    @Select("SELECT i.*, p.name as product_name, p.code as product_code FROM sale_order_item i " +
            "LEFT JOIN product p ON i.product_id = p.id WHERE i.order_id = #{orderId}")
    List<SaleOrderItem> selectByOrderId(Long orderId);

    @Delete("DELETE FROM sale_order_item WHERE order_id = #{orderId}")
    void deleteByOrderId(Long orderId);

    /** 产品销售排行 */
    @Select("SELECT p.id as product_id, p.name as product_name, p.code as product_code, " +
            "COUNT(DISTINCT i.order_id) as order_count, " +
            "COALESCE(SUM(i.quantity), 0) as total_quantity, " +
            "COALESCE(SUM(i.amount), 0) as total_amount " +
            "FROM sale_order_item i " +
            "LEFT JOIN product p ON i.product_id = p.id " +
            "LEFT JOIN sale_order o ON i.order_id = o.id " +
            "WHERE o.create_time >= #{start} AND o.create_time < #{end} " +
            "GROUP BY p.id, p.name, p.code ORDER BY total_amount DESC LIMIT #{limit}")
    List<Map<String, Object>> selectProductRanking(@Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end,
                                                              @Param("limit") int limit);
}
