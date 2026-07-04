package com.itheima.mes1.module.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.sale.entity.DeliveryItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DeliveryItemMapper extends BaseMapper<DeliveryItem> {
    @Select("SELECT di.*, p.name as product_name, p.code as product_code " +
            "FROM delivery_item di LEFT JOIN product p ON di.product_id = p.id " +
            "WHERE di.delivery_id = #{deliveryId}")
    List<DeliveryItem> selectByDeliveryId(Long deliveryId);

    @Delete("DELETE FROM delivery_item WHERE delivery_id = #{deliveryId}")
    void deleteByDeliveryId(Long deliveryId);
}
