package com.itheima.mes1.module.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.production.entity.WorkReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkReportMapper extends BaseMapper<WorkReport> {
    @Select("SELECT wr.*, wo.order_no as work_order_no, p.name as process_name " +
            "FROM work_report wr " +
            "LEFT JOIN work_order wo ON wr.work_order_id = wo.id " +
            "LEFT JOIN work_order_process wop ON wr.work_order_process_id = wop.id " +
            "LEFT JOIN process p ON wop.process_id = p.id " +
            "WHERE wr.id = #{id}")
    WorkReport selectWithRelations(Long id);
}
