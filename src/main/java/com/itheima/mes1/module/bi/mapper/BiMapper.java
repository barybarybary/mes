package com.itheima.mes1.module.bi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BiMapper {

    // ==================== 经营概览 ====================

    /** 本月销售额 */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM sale_order " +
            "WHERE create_time >= #{start} AND create_time < #{end}")
    BigDecimal selectMonthSales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 本月订单数 */
    @Select("SELECT COUNT(*) FROM sale_order " +
            "WHERE create_time >= #{start} AND create_time < #{end}")
    long selectMonthOrderCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 本月毛利额 (基于产品成本价) */
    @Select("SELECT COALESCE(SUM(i.amount - i.quantity * COALESCE(p.cost_price, 0)), 0) " +
            "FROM sale_order_item i " +
            "LEFT JOIN product p ON i.product_id = p.id " +
            "LEFT JOIN sale_order o ON i.order_id = o.id " +
            "WHERE o.create_time >= #{start} AND o.create_time < #{end}")
    BigDecimal selectMonthGrossProfit(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ==================== 月度趋势 ====================

    /** 按月统计销售额（含上月和去年同月用于同环比计算） */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') as month, " +
            "COALESCE(SUM(total_amount), 0) as amount, COUNT(*) as count " +
            "FROM sale_order WHERE create_time >= #{start} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> selectMonthlySales(@Param("start") LocalDateTime start);

    // ==================== 产品毛利排行 ====================

    @Select("SELECT p.id as product_id, p.name as product_name, p.code as product_code, " +
            "COALESCE(SUM(i.quantity), 0) as total_quantity, " +
            "COALESCE(SUM(i.amount), 0) as total_amount, " +
            "COALESCE(p.cost_price, 0) as cost_price, " +
            "COALESCE(SUM(i.quantity * COALESCE(p.cost_price, 0)), 0) as total_cost, " +
            "COALESCE(SUM(i.amount - i.quantity * COALESCE(p.cost_price, 0)), 0) as gross_profit " +
            "FROM sale_order_item i " +
            "LEFT JOIN product p ON i.product_id = p.id " +
            "LEFT JOIN sale_order o ON i.order_id = o.id " +
            "WHERE o.create_time >= #{start} AND o.create_time < #{end} " +
            "GROUP BY p.id, p.name, p.code, p.cost_price " +
            "ORDER BY gross_profit DESC LIMIT #{limit}")
    List<Map<String, Object>> selectProductProfit(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("limit") int limit);

    // ==================== 客户价值排行 ====================

    @Select("SELECT c.id as customer_id, c.name as customer_name, " +
            "COUNT(so.id) as order_count, " +
            "COALESCE(SUM(so.total_amount), 0) as total_amount, " +
            "ROUND(COALESCE(SUM(so.total_amount), 0) / NULLIF(COUNT(so.id), 0), 2) as avg_order " +
            "FROM sale_order so LEFT JOIN customer c ON so.customer_id = c.id " +
            "WHERE so.create_time >= #{start} AND so.create_time < #{end} " +
            "GROUP BY c.id, c.name ORDER BY total_amount DESC LIMIT #{limit}")
    List<Map<String, Object>> selectCustomerValue(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("limit") int limit);

    // ==================== 多维交叉: 时间×产品 ====================

    @Select("<script>" +
            "SELECT p.id as product_id, p.name as product_name, " +
            "COALESCE(SUM(i.quantity), 0) as quantity, " +
            "COALESCE(SUM(i.amount), 0) as amount " +
            "FROM sale_order_item i " +
            "LEFT JOIN product p ON i.product_id = p.id " +
            "LEFT JOIN sale_order o ON i.order_id = o.id " +
            "WHERE 1=1 " +
            "<if test='year != null'>AND YEAR(o.create_time) = #{year}</if> " +
            "<if test='month != null'>AND MONTH(o.create_time) = #{month}</if> " +
            "GROUP BY p.id, p.name ORDER BY amount DESC LIMIT 20" +
            "</script>")
    List<Map<String, Object>> pivotSalesByProduct(@Param("year") Integer year,
                                                   @Param("month") Integer month);

    /** 时间×客户 */
    @Select("<script>" +
            "SELECT c.id as customer_id, c.name as customer_name, " +
            "COUNT(so.id) as order_count, " +
            "COALESCE(SUM(so.total_amount), 0) as amount " +
            "FROM sale_order so LEFT JOIN customer c ON so.customer_id = c.id " +
            "WHERE 1=1 " +
            "<if test='year != null'>AND YEAR(so.create_time) = #{year}</if> " +
            "<if test='month != null'>AND MONTH(so.create_time) = #{month}</if> " +
            "GROUP BY c.id, c.name ORDER BY amount DESC LIMIT 20" +
            "</script>")
    List<Map<String, Object>> pivotSalesByCustomer(@Param("year") Integer year,
                                                    @Param("month") Integer month);

    /** 月份×产品类别矩阵 */
    @Select("<script>" +
            "SELECT pc.id as category_id, pc.name as category_name, " +
            "DATE_FORMAT(o.create_time, '%Y-%m') as month, " +
            "COALESCE(SUM(i.amount), 0) as amount " +
            "FROM sale_order_item i " +
            "LEFT JOIN sale_order o ON i.order_id = o.id " +
            "LEFT JOIN product p ON i.product_id = p.id " +
            "LEFT JOIN product_category pc ON p.category_id = pc.id " +
            "WHERE o.create_time >= #{start} AND o.create_time &lt; #{end} " +
            "GROUP BY pc.id, pc.name, DATE_FORMAT(o.create_time, '%Y-%m') " +
            "ORDER BY month, amount DESC" +
            "</script>")
    List<Map<String, Object>> pivotSalesByMonthCategory(@Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    /** 仓库×产品类别库存 */
    @Select("SELECT w.id as warehouse_id, w.name as warehouse_name, " +
            "pc.id as category_id, pc.name as category_name, " +
            "COALESCE(SUM(i.quantity), 0) as quantity, COUNT(DISTINCT i.product_id) as sku_count " +
            "FROM inventory i " +
            "LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
            "LEFT JOIN product p ON i.product_id = p.id " +
            "LEFT JOIN product_category pc ON p.category_id = pc.id " +
            "WHERE i.quantity > 0 " +
            "GROUP BY w.id, w.name, pc.id, pc.name ORDER BY w.name, quantity DESC")
    List<Map<String, Object>> pivotInventoryByWarehouse();

    /** 月份×产品产量 */
    @Select("<script>" +
            "SELECT p.id as product_id, p.name as product_name, " +
            "DATE_FORMAT(wo.actual_end, '%Y-%m') as month, " +
            "COALESCE(SUM(wo.finished_qty), 0) as quantity " +
            "FROM work_order wo LEFT JOIN product p ON wo.product_id = p.id " +
            "WHERE wo.status >= 3 AND wo.actual_end IS NOT NULL " +
            "<if test='start != null'>AND wo.actual_end >= #{start}</if> " +
            "<if test='end != null'>AND wo.actual_end &lt; #{end}</if> " +
            "GROUP BY p.id, p.name, DATE_FORMAT(wo.actual_end, '%Y-%m') " +
            "ORDER BY month, quantity DESC" +
            "</script>")
    List<Map<String, Object>> pivotProductionByMonth(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    /** 客户×月份发货量 */
    @Select("<script>" +
            "SELECT c.id as customer_id, c.name as customer_name, " +
            "DATE_FORMAT(d.delivery_date, '%Y-%m') as month, " +
            "COALESCE(SUM(di.quantity), 0) as quantity " +
            "FROM delivery_item di " +
            "LEFT JOIN delivery d ON di.delivery_id = d.id " +
            "LEFT JOIN customer c ON d.customer_id = c.id " +
            "WHERE d.status >= 2 " +
            "<if test='start != null'>AND d.delivery_date >= #{start}</if> " +
            "<if test='end != null'>AND d.delivery_date &lt; #{end}</if> " +
            "GROUP BY c.id, c.name, DATE_FORMAT(d.delivery_date, '%Y-%m') " +
            "ORDER BY month, quantity DESC" +
            "</script>")
    List<Map<String, Object>> pivotDeliveryByCustomer(@Param("start") LocalDate start,
                                                       @Param("end") LocalDate end);

    // ==================== 告警扫描 ====================

    /** 库存低于安全线 */
    @Select("SELECT i.product_id, p.name as product_name, w.name as warehouse_name, " +
            "i.quantity, '库存低于安全线(10)' as alert_title " +
            "FROM inventory i LEFT JOIN product p ON i.product_id = p.id " +
            "LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
            "WHERE i.quantity > 0 AND i.quantity < 10")
    List<Map<String, Object>> scanLowStock();

    /** 订单超期未交付 */
    @Select("SELECT so.id, so.order_no, c.name as customer_name, so.delivery_date, so.status, " +
            "CONCAT('订单', so.order_no, '交期', so.delivery_date, '已超期') as alert_title " +
            "FROM sale_order so LEFT JOIN customer c ON so.customer_id = c.id " +
            "WHERE so.delivery_date < CURDATE() AND so.status NOT IN (5, 6)")
    List<Map<String, Object>> scanOverdueOrders();

    /** 质检不合格率 */
    @Select("SELECT qc.product_id, p.name as product_name, " +
            "SUM(qc.check_qty) as total_check, SUM(qc.ng_qty) as total_ng, " +
            "ROUND(SUM(qc.ng_qty) / NULLIF(SUM(qc.check_qty), 0) * 100, 1) as ng_rate, " +
            "CONCAT('质检不合格率 ', ROUND(SUM(qc.ng_qty) / NULLIF(SUM(qc.check_qty), 0) * 100, 1), '% 超标') as alert_title " +
            "FROM qc_record qc LEFT JOIN product p ON qc.product_id = p.id " +
            "WHERE qc.check_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "GROUP BY qc.product_id, p.name " +
            "HAVING ng_rate > 5")
    List<Map<String, Object>> scanQcNgRate();
}