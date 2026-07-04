package com.itheima.mes1.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import java.util.List;

@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {
    @Select("SELECT so.*, c.name as customer_name FROM sale_order so LEFT JOIN customer c ON so.customer_id = c.id " +
            "WHERE so.id = #{id}")
    SaleOrder selectWithCustomer(Long id);

    @Update("UPDATE sale_order SET status = #{status} WHERE id = #{id}")
    void updateStatus(Long id, Integer status);

    /** 按日期统计销售额和订单数（优化版：一次查询替代 N 次循环） */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count, COALESCE(SUM(total_amount), 0) as amount " +
            "FROM sale_order WHERE create_time >= #{start} AND create_time < #{end} " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> selectDailyStats(@Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    /** 客户销售排行 */
    @Select("SELECT c.id as customer_id, c.name as customer_name, " +
            "COUNT(so.id) as order_count, COALESCE(SUM(so.total_amount), 0) as total_amount " +
            "FROM sale_order so LEFT JOIN customer c ON so.customer_id = c.id " +
            "WHERE so.create_time >= #{start} AND so.create_time < #{end} " +
            "GROUP BY c.id, c.name ORDER BY total_amount DESC LIMIT #{limit}")
    List<Map<String, Object>> selectCustomerRanking(@Param("start") LocalDateTime start,
                                                               @Param("end") LocalDateTime end,
                                                               @Param("limit") int limit);
}
