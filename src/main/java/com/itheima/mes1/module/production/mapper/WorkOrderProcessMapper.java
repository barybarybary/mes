package com.itheima.mes1.module.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.production.entity.WorkOrderProcess;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WorkOrderProcessMapper extends BaseMapper<WorkOrderProcess> {
    @Select("SELECT wp.*, pr.name as process_name FROM work_order_process wp " +
            "LEFT JOIN process pr ON wp.process_id = pr.id WHERE wp.work_order_id = #{workOrderId} ORDER BY wp.sort")
    List<WorkOrderProcess> selectByWorkOrderId(Long workOrderId);

    @Delete("DELETE FROM work_order_process WHERE work_order_id = #{workOrderId}")
    void deleteByWorkOrderId(Long workOrderId);
}
