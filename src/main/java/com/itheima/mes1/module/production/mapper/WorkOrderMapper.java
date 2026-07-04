package com.itheima.mes1.module.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.production.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {
    @Select("SELECT wo.*, p.name as product_name, p.code as product_code FROM work_order wo " +
            "LEFT JOIN product p ON wo.product_id = p.id WHERE wo.id = #{id}")
    WorkOrder selectWithProduct(Long id);
}
