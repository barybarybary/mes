package com.itheima.mes1.module.base.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.module.base.entity.Bom;
import com.itheima.mes1.module.base.entity.Product;
import com.itheima.mes1.module.base.mapper.BomMapper;
import com.itheima.mes1.module.base.mapper.ProductMapper;
import com.itheima.mes1.module.base.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private BomMapper bomMapper;

    @Override
    public Page<Product> pageProducts(int page, int pageSize, String keyword, Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StrUtil.isNotBlank(keyword), Product::getName, keyword)
                .or().like(StrUtil.isNotBlank(keyword), Product::getCode, keyword)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .orderByDesc(Product::getCreateTime);
        Page<Product> result = page(new Page<>(page, pageSize), wrapper);
        result.setTotal(count(wrapper));
        return result;
    }

    @Override
    public Product getDetail(Long id) {
        Product product = getById(id);
        if (product != null) {
            product.setBomList(bomMapper.selectByProductId(id));
        }
        return product;
    }

    @Override
    @Transactional
    public void saveBoms(Long productId, java.util.List<Bom> bomList) {
        // 先删后加
        bomMapper.delete(new LambdaQueryWrapper<Bom>().eq(Bom::getProductId, productId));
        for (Bom bom : bomList) {
            bom.setProductId(productId);
            bomMapper.insert(bom);
        }
    }
}
