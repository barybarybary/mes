package com.itheima.mes1.module.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.production.entity.QcStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QcStandardMapper extends BaseMapper<QcStandard> {

    @Select("SELECT qs.*, p.name as product_name, pr.name as process_name " +
            "FROM qc_standard qs " +
            "LEFT JOIN product p ON qs.product_id = p.id " +
            "LEFT JOIN process pr ON qs.process_id = pr.id " +
            "ORDER BY qs.product_id, qs.sort_order")
    List<QcStandard> selectWithNames();
}
