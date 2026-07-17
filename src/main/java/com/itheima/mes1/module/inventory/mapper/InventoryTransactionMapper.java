package com.itheima.mes1.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.inventory.entity.InventoryTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {

    /** 指定时间段内出库总量（取绝对值） */
    @Select("SELECT COALESCE(SUM(ABS(quantity)), 0) FROM inventory_transaction " +
            "WHERE type = 'out' AND create_time >= #{start} AND create_time < #{end}")
    BigDecimal sumOutboundQuantity(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间段内入库总量 */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM inventory_transaction " +
            "WHERE type = 'in' AND create_time >= #{start} AND create_time < #{end}")
    BigDecimal sumInboundQuantity(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 指定时间段内按仓库出库总量（取绝对值） */
    @Select("SELECT COALESCE(SUM(ABS(quantity)), 0) FROM inventory_transaction " +
            "WHERE type = 'out' AND warehouse_id = #{warehouseId} AND create_time >= #{start}")
    BigDecimal sumOutboundByWarehouse(@Param("warehouseId") Long warehouseId,
                                       @Param("start") LocalDateTime start);
}
