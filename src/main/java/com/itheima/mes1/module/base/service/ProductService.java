package com.itheima.mes1.module.base.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.mes1.module.base.entity.Bom;
import com.itheima.mes1.module.base.entity.Product;
import java.util.List;

public interface ProductService extends IService<Product> {
    Page<Product> pageProducts(int page, int pageSize, String keyword, Long categoryId);
    Product getDetail(Long id);
    void saveBoms(Long productId, List<Bom> bomList);
}
