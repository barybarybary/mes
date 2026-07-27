package com.itheima.mes1.module.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.module.portal.dto.PlaceOrderReq;
import com.itheima.mes1.module.portal.dto.PortalLoginReq;
import com.itheima.mes1.module.portal.dto.PortalRegisterReq;
import com.itheima.mes1.module.portal.service.PortalService;
import com.itheima.mes1.module.portal.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "客户门户")
@RestController
@RequestMapping("/api/portal")
public class PortalController {

    @Autowired
    private PortalService portalService;

    // ==================== 认证 ====================

    @Operation(summary = "客户注册")
    @PostMapping("/register")
    public Result<PortalLoginVO> register(@RequestBody PortalRegisterReq req) {
        return Result.ok(portalService.register(req));
    }

    @Operation(summary = "客户登录")
    @PostMapping("/login")
    public Result<PortalLoginVO> login(@RequestBody PortalLoginReq req) {
        return Result.ok(portalService.login(req));
    }

    // ==================== 产品浏览 ====================

    @Operation(summary = "产品列表")
    @GetMapping("/products")
    public Result<PageResult<ProductCatalogVO>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        Page<ProductCatalogVO> result = portalService.listProducts(page, pageSize, keyword, categoryId);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "产品分类")
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> listCategories() {
        return Result.ok(portalService.listCategories());
    }

    @Operation(summary = "产品详情")
    @GetMapping("/products/{id}")
    public Result<ProductDetailVO> getProduct(@PathVariable Long id) {
        return Result.ok(portalService.getProductDetail(id));
    }

    // ==================== 购物车 ====================

    @Operation(summary = "加入购物车")
    @PostMapping("/cart/add")
    public Result<?> addToCart(@RequestAttribute("portalCustomerId") Long customerId,
                               @RequestParam Long productId,
                               @RequestParam(defaultValue = "1") int quantity) {
        portalService.addToCart(customerId, productId, quantity);
        return Result.ok();
    }

    @Operation(summary = "查看购物车")
    @GetMapping("/cart")
    public Result<List<Map<String, Object>>> getCart(@RequestAttribute("portalCustomerId") Long customerId) {
        return Result.ok(portalService.getCart(customerId));
    }

    @Operation(summary = "修改购物车")
    @PutMapping("/cart/{productId}")
    public Result<?> updateCart(@RequestAttribute("portalCustomerId") Long customerId,
                                @PathVariable Long productId,
                                @RequestParam int quantity) {
        portalService.updateCartItem(customerId, productId, quantity);
        return Result.ok();
    }

    // ==================== 订单 ====================

    @Operation(summary = "提交订单")
    @PostMapping("/orders")
    public Result<PortalOrderVO> placeOrder(@RequestAttribute("portalCustomerId") Long customerId,
                                            @RequestBody PlaceOrderReq req) {
        return Result.ok(portalService.placeOrder(customerId, req));
    }

    @Operation(summary = "订单列表")
    @GetMapping("/orders")
    public Result<PageResult<PortalOrderVO>> listOrders(
            @RequestAttribute("portalCustomerId") Long customerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Page<PortalOrderVO> result = portalService.listOrders(customerId, page, pageSize, status);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/orders/{id}")
    public Result<PortalOrderVO> getOrder(@RequestAttribute("portalCustomerId") Long customerId,
                                          @PathVariable Long id) {
        return Result.ok(portalService.getOrderDetail(customerId, id));
    }

    @Operation(summary = "模拟支付")
    @PutMapping("/orders/{id}/pay")
    public Result<?> payOrder(@RequestAttribute("portalCustomerId") Long customerId,
                               @PathVariable Long id) {
        portalService.payOrder(customerId, id);
        return Result.ok("支付成功");
    }

    @Operation(summary = "取消订单")
    @PutMapping("/orders/{id}/cancel")
    public Result<?> cancelOrder(@RequestAttribute("portalCustomerId") Long customerId,
                                  @PathVariable Long id) {
        portalService.cancelOrder(customerId, id);
        return Result.ok("订单已取消");
    }

    // ==================== 个人中心 ====================

    @Operation(summary = "个人统计")
    @GetMapping("/profile/stats")
    public Result<Map<String, Object>> profileStats(@RequestAttribute("portalCustomerId") Long customerId) {
        return Result.ok(portalService.getProfileStats(customerId));
    }

    @Operation(summary = "个人资料")
    @GetMapping("/profile")
    public Result<CustomerVO> getProfile(@RequestAttribute("portalCustomerId") Long customerId) {
        return Result.ok(portalService.getProfile(customerId));
    }

    @Operation(summary = "修改资料")
    @PutMapping("/profile")
    public Result<CustomerVO> updateProfile(@RequestAttribute("portalCustomerId") Long customerId,
                                            @RequestBody Map<String, String> body) {
        return Result.ok(portalService.updateProfile(customerId, body));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<?> changePassword(@RequestAttribute("portalCustomerId") Long customerId,
                                    @RequestBody Map<String, String> body) {
        portalService.changePassword(customerId,
                body.get("oldPassword"), body.get("newPassword"));
        return Result.ok("密码修改成功");
    }
}
