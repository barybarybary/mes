package com.itheima.mes1.module.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.production.entity.QcRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QcRecordMapper extends BaseMapper<QcRecord> {

    @Select("<script>" +
            "SELECT wop.id, p.name " +
            "FROM work_order_process wop LEFT JOIN process p ON wop.process_id = p.id " +
            "WHERE wop.id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<java.util.Map<String, Object>> selectProcessNames(@Param("ids") List<Long> ids);
}
