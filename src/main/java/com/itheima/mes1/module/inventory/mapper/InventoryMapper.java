package com.itheima.mes1.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
    @Select("SELECT i.*, p.name as product_name, p.code as product_code, w.name as warehouse_name " +
            "FROM inventory i LEFT JOIN product p ON i.product_id = p.id LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
            "WHERE i.quantity > 0 ORDER BY i.update_time DESC")
    List<Inventory> selectAllWithDetail();

    @Select("SELECT i.*, p.name as product_name, p.code as product_code, w.name as warehouse_name " +
            "FROM inventory i LEFT JOIN product p ON i.product_id = p.id LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
            "WHERE i.product_id = #{productId} AND i.quantity > 0 ORDER BY i.update_time DESC")
    List<Inventory> selectByProduct(Long productId);

    /** 当前库存总数量（按仓库分组） */
    @Select("SELECT w.id as warehouse_id, w.name as warehouse_name, " +
            "COALESCE(SUM(i.quantity), 0) as total_quantity, COUNT(DISTINCT i.product_id) as sku_count " +
            "FROM inventory i LEFT JOIN warehouse w ON i.warehouse_id = w.id " +
            "WHERE i.quantity > 0 GROUP BY w.id, w.name ORDER BY total_quantity DESC")
    List<Map<String, Object>> selectWarehouseStructure();

    /** 当前库存平均量（用于周转率计算） */
    @Select("SELECT COALESCE(AVG(quantity), 0) FROM inventory WHERE quantity > 0")
    BigDecimal selectAverageStock();

    /** 某产品总库存量 */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM inventory WHERE product_id = #{productId}")
    Integer sumQuantityByProduct(Long productId);

    /** 批量查询产品库存 */
    @Select("<script>" +
            "SELECT product_id, COALESCE(SUM(quantity), 0) as stock " +
            "FROM inventory WHERE product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            " GROUP BY product_id" +
            "</script>")
    List<Map<String, Object>> sumQuantityByProductIds(@org.apache.ibatis.annotations.Param("productIds") List<Long> productIds);
}
