package com.itheima.mes1.module.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.production.entity.ProcessSop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProcessSopMapper extends BaseMapper<ProcessSop> {

    @Select("SELECT ps.*, p.name as process_name, d.title as doc_title " +
            "FROM process_sop ps " +
            "LEFT JOIN process p ON ps.process_id = p.id " +
            "LEFT JOIN kb_document d ON ps.kb_document_id = d.id " +
            "ORDER BY ps.process_id, ps.sort_order")
    List<ProcessSop> selectWithNames();

    @Select("SELECT ps.*, p.name as process_name, d.title as doc_title " +
            "FROM process_sop ps " +
            "LEFT JOIN process p ON ps.process_id = p.id " +
            "LEFT JOIN kb_document d ON ps.kb_document_id = d.id " +
            "WHERE ps.process_id = #{processId} " +
            "ORDER BY ps.sort_order")
    List<ProcessSop> selectByProcessId(Long processId);
}
