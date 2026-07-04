package com.itheima.mes1.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.base.entity.Bom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BomMapper extends BaseMapper<Bom> {
    @Select("SELECT b.*, p.name as material_name, p.code as material_code, pr.name as process_name " +
            "FROM bom b LEFT JOIN product p ON b.material_id = p.id LEFT JOIN process pr ON b.process_id = pr.id " +
            "WHERE b.product_id = #{productId} ORDER BY b.sort")
    List<Bom> selectByProductId(Long productId);
}
