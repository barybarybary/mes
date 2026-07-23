package com.itheima.mes1.module.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.report.entity.ReportSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportScheduleMapper extends BaseMapper<ReportSchedule> {
    @Select("SELECT * FROM report_schedule WHERE status = 1")
    List<ReportSchedule> selectAllActive();
}
