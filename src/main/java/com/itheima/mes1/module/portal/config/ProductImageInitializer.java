package com.itheima.mes1.module.portal.config;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.mes1.module.base.entity.Product;
import com.itheima.mes1.module.base.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 为没有图片的产品随机分配占位图（仅首次运行一次）
 */
@Slf4j
@Component
public class ProductImageInitializer implements CommandLineRunner {

    private static final String[] PLACEHOLDER_IMAGES = {
        "/images/products/steel-material.svg",
        "/images/products/gear-parts.svg",
        "/images/products/electronic-parts.svg",
        "/images/products/plastic-parts.svg",
        "/images/products/chemical-material.svg",
        "/images/products/hardware-tools.svg",
    };

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void run(String... args) {
        try {
            List<Product> products = productMapper.selectList(null);
            int updated = 0;
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                if (p.getImageUrl() == null || p.getImageUrl().isBlank()) {
                    String img = PLACEHOLDER_IMAGES[i % PLACEHOLDER_IMAGES.length];
                    productMapper.update(null,
                        new LambdaUpdateWrapper<Product>()
                            .eq(Product::getId, p.getId())
                            .set(Product::getImageUrl, img));
                    updated++;
                }
            }
            if (updated > 0) {
                log.info("已为 {} 个产品分配占位图片", updated);
            }
        } catch (Exception e) {
            log.warn("产品图片初始化跳过: {}", e.getMessage());
        }
    }
}
