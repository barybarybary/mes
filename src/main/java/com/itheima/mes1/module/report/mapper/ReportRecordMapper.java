package com.itheima.mes1.module.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.report.entity.ReportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportRecordMapper extends BaseMapper<ReportRecord> {
    @Select("SELECT * FROM report_record WHERE id = #{id}")
    ReportRecord selectWithBytes(Long id);

    @Select("SELECT id, user_id, title, report_type, time_range, file_name, file_size, create_time " +
            "FROM report_record WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ReportRecord> selectByUserId(Long userId, int limit);
}
