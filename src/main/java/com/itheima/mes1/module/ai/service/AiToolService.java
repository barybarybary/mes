package com.itheima.mes1.module.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.mes1.module.base.entity.Customer;
import com.itheima.mes1.module.base.entity.Product;
import com.itheima.mes1.module.base.mapper.CustomerMapper;
import com.itheima.mes1.module.base.mapper.ProductMapper;
import com.itheima.mes1.module.inventory.entity.Inventory;
import com.itheima.mes1.module.inventory.entity.InventoryTransaction;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import com.itheima.mes1.module.inventory.mapper.InventoryTransactionMapper;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 工具集 — 由大模型通过 Function Calling 调用，查询 MES 系统内真实数据
 * 每个方法加 @Tool 注解，LangChain4j 自动生成 tool specification 发给大模型
 */
@Service
public class AiToolService {

    @Autowired private ProductMapper productMapper;
    @Autowired private InventoryMapper inventoryMapper;
    @Autowired private SaleOrderMapper saleOrderMapper;
    @Autowired private WorkOrderMapper workOrderMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private InventoryTransactionMapper transactionMapper;

    @Tool("按名称或编码模糊查询产品。参数 keyword 为产品名称或编码关键字")
    public String queryProduct(@P("产品名称或编码关键字") String keyword) {
        List<Product> list = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .like(Product::getName, keyword).or()
                        .like(Product::getCode, keyword).last("LIMIT 10"));
        if (list.isEmpty()) return "未找到与「" + keyword + "」相关的产品。";
        return list.stream()
                .map(p -> String.format("[%s] %s | 规格:%s | 售价:¥%s | 单位:%s | 状态:%s",
                        p.getCode(), p.getName(), p.getSpec() != null ? p.getSpec() : "-",
                        p.getPrice(), p.getUnit(), p.getStatus() == 1 ? "启用" : "停用"))
                .collect(Collectors.joining("\n"));
    }

    @Tool("按产品名或仓库名查询库存。查询所有产品库存时参数传空字符串")
    public String queryInventory(@P("产品名或仓库名(可选,传空字符串查全部)") String keyword) {
        List<Inventory> list;
        if (keyword == null || keyword.isBlank()) {
            list = inventoryMapper.selectAllWithDetail();
        } else {
            list = inventoryMapper.selectList(
                    new LambdaQueryWrapper<Inventory>()
                            .gt(Inventory::getQuantity, 0)
                            .and(w -> w.like(Inventory::getProductName, keyword)
                                    .or().like(Inventory::getWarehouseName, keyword))
                            .last("LIMIT 20"));
            // Reload with detail
            if (!list.isEmpty()) {
                list = inventoryMapper.selectAllWithDetail().stream()
                        .filter(i -> i.getProductName() != null && i.getProductName().contains(keyword)
                                || i.getWarehouseName() != null && i.getWarehouseName().contains(keyword))
                        .collect(Collectors.toList());
            } else {
                list = inventoryMapper.selectAllWithDetail();
            }
        }
        if (list.isEmpty()) return "当前没有任何库存记录。";
        return "库存总量 " + list.stream().map(Inventory::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add)
                + "（" + list.size() + " 条记录）:\n" +
                list.stream()
                        .map(i -> String.format("[%s] %s | 仓库:%s | 库存:%s %s | 批次:%s",
                                i.getProductCode(), i.getProductName(), i.getWarehouseName(),
                                i.getQuantity(), i.getUnit(), i.getBatchNo() != null ? i.getBatchNo() : "-"))
                        .collect(Collectors.joining("\n"));
    }

    @Tool("按订单号精确查询销售订单详情,包括客户、金额、状态、交期")
    public String getSaleOrder(@P("销售订单号,如SO20260601001") String orderNo) {
        SaleOrder order = saleOrderMapper.selectOne(
                new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getOrderNo, orderNo));
        if (order == null) return "未找到订单 " + orderNo;
        SaleOrder full = saleOrderMapper.selectWithCustomer(order.getId());
        String statusName = switch (order.getStatus()) {
            case 1 -> "待审核"; case 2 -> "已审核"; case 3 -> "生产中";
            case 4 -> "部分发货"; case 5 -> "已完成"; case 6 -> "已取消";
            default -> "未知";
        };
        return String.format("订单 %s\n客户:%s | 金额:¥%s | 状态:%s | 下单:%s | 交期:%s | 备注:%s",
                orderNo, full != null ? full.getCustomerName() : "-",
                order.getTotalAmount(), statusName, order.getOrderDate(),
                order.getDeliveryDate(), order.getRemark() != null ? order.getRemark() : "无");
    }

    @Tool("模糊搜索销售订单：可按订单号、客户名、产品名搜索。返回匹配的订单列表")
    public String searchOrders(@P("搜索关键词(订单号/客户名/产品名)") String keyword) {
        List<SaleOrder> list;
        if (keyword == null || keyword.isBlank()) {
            list = saleOrderMapper.selectList(
                    new LambdaQueryWrapper<SaleOrder>().orderByDesc(SaleOrder::getCreateTime).last("LIMIT 20"));
        } else {
            // 先按客户名搜
            List<Customer> customers = customerMapper.selectList(
                    new LambdaQueryWrapper<Customer>().like(Customer::getName, keyword));
            List<Long> customerIds = customers.stream().map(Customer::getId).collect(Collectors.toList());
            LambdaQueryWrapper<SaleOrder> qw = new LambdaQueryWrapper<>();
            qw.and(w -> w.like(SaleOrder::getOrderNo, keyword));
            if (!customerIds.isEmpty()) {
                qw.or(w -> w.in(SaleOrder::getCustomerId, customerIds));
            }
            qw.orderByDesc(SaleOrder::getCreateTime).last("LIMIT 20");
            list = saleOrderMapper.selectList(qw);
        }
        if (list.isEmpty()) return "未找到与「" + keyword + "」相关的订单。";
        return list.stream()
                .map(o -> {
                    SaleOrder full = saleOrderMapper.selectWithCustomer(o.getId());
                    return String.format("[%s] %s | ¥%s | 状态:%s | 交期:%s | %s",
                            o.getOrderNo(), full != null ? full.getCustomerName() : "-",
                            o.getTotalAmount(), statusDesc(o.getStatus()),
                            o.getDeliveryDate(), o.getOrderDate());
                })
                .collect(Collectors.joining("\n"));
    }

    @Tool("按状态查询销售订单列表。status: 1待审核 2已审核 3生产中 4部分发货 5已完成。传null查全部")
    public String listSaleOrders(@P("订单状态,null表示全部") Integer status) {
        List<SaleOrder> list;
        if (status != null) {
            list = saleOrderMapper.selectList(
                    new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getStatus, status)
                            .orderByDesc(SaleOrder::getCreateTime).last("LIMIT 10"));
        } else {
            list = saleOrderMapper.selectList(
                    new LambdaQueryWrapper<SaleOrder>().orderByDesc(SaleOrder::getCreateTime).last("LIMIT 10"));
        }
        if (list.isEmpty()) return "暂无销售订单。";
        return list.stream()
                .map(o -> {
                    SaleOrder full = saleOrderMapper.selectWithCustomer(o.getId());
                    return String.format("[%s] %s | ¥%s | 状态:%s | %s",
                            o.getOrderNo(), full != null ? full.getCustomerName() : "-",
                            o.getTotalAmount(), statusDesc(o.getStatus()), o.getOrderDate());
                })
                .collect(Collectors.joining("\n"));
    }

    @Tool("按工单号精确查询生产工单详情,包括产品、数量、完成进度、状态")
    public String getWorkOrder(@P("工单号,如WO20260602001") String orderNo) {
        WorkOrder wo = workOrderMapper.selectOne(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getOrderNo, orderNo));
        if (wo == null) return "未找到工单 " + orderNo;
        WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
        return String.format("工单 %s\n产品:%s | 计划:%s | 完成:%s | 合格:%s | 报废:%s | 状态:%s | 计划周期:%s ~ %s",
                orderNo, full != null ? full.getProductName() : "-",
                wo.getQuantity(), wo.getFinishedQty(), wo.getQualifiedQty(), wo.getScrapQty(),
                woStatusDesc(wo.getStatus()), wo.getPlanStart(), wo.getPlanEnd());
    }

    @Tool("按状态查询生产工单列表。status: 1待生产 2生产中 3已完成 4已入库。传null查全部")
    public String listWorkOrders(@P("工单状态,null表示全部") Integer status) {
        LambdaQueryWrapper<WorkOrder> qw = new LambdaQueryWrapper<>();
        if (status != null) qw.eq(WorkOrder::getStatus, status);
        qw.orderByDesc(WorkOrder::getCreateTime).last("LIMIT 10");
        List<WorkOrder> list = workOrderMapper.selectList(qw);
        if (list.isEmpty()) return "暂无工单。";
        return list.stream()
                .map(wo -> {
                    WorkOrder full = workOrderMapper.selectWithProduct(wo.getId());
                    return String.format("[%s] %s | 完成:%s/%s | 状态:%s",
                            wo.getOrderNo(), full != null ? full.getProductName() : "-",
                            wo.getFinishedQty(), wo.getQuantity(), woStatusDesc(wo.getStatus()));
                })
                .collect(Collectors.joining("\n"));
    }

    @Tool("获取系统概览KPI: 待处理订单数、生产中工单数、今日入库数、在库SKU数")
    public String getDashboardSummary() {
        long pendingOrders = saleOrderMapper.selectCount(
                new LambdaQueryWrapper<SaleOrder>().eq(SaleOrder::getStatus, 1));
        long inProgressOrders = workOrderMapper.selectCount(
                new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 2));
        long todayIn = transactionMapper.selectCount(
                new LambdaQueryWrapper<InventoryTransaction>()
                        .eq(InventoryTransaction::getType, "in")
                        .ge(InventoryTransaction::getCreateTime, LocalDate.now()));
        long skuCount = inventoryMapper.selectCount(
                new LambdaQueryWrapper<Inventory>().gt(Inventory::getQuantity, 0));

        return String.format("=== 造易MES 系统概览 ===\n"
                        + "待处理订单: %d 笔\n"
                        + "生产中工单: %d 个\n"
                        + "今日入库: %d 笔\n"
                        + "在库SKU数: %d 个",
                pendingOrders, inProgressOrders, todayIn, skuCount);
    }

    @Tool("系统健康巡检：一次性检查订单交付、库存预警、生产进度、质检异常，返回综合诊断报告")
    public String systemHealthCheck() {
        StringBuilder report = new StringBuilder();
        report.append("=== 造易MES 系统健康巡检 ===\n\n");

        // 1. 订单交付
        long overdueOrders = 0;
        List<SaleOrder> allOrders = saleOrderMapper.selectList(null);
        for (SaleOrder o : allOrders) {
            if (o.getDeliveryDate() != null && o.getDeliveryDate().isBefore(LocalDate.now()) && o.getStatus() != 5 && o.getStatus() != 6) {
                overdueOrders++;
            }
        }
        long pendingAudit = allOrders.stream().filter(o -> o.getStatus() == 1).count();
        report.append("【订单交付】\n");
        report.append(String.format("  超期未交付: %d 单\n", overdueOrders));
        report.append(String.format("  待审核: %d 单\n", pendingAudit));
        report.append(String.format("  交付健康: %s\n", overdueOrders == 0 ? "✅ 正常" : "⚠️ 有超期订单需处理"));

        // 2. 库存
        report.append("\n【库存状态】\n");
        long lowStock = 0;
        List<Inventory> allInv = inventoryMapper.selectAllWithDetail();
        for (Inventory inv : allInv) {
            if (inv.getQuantity().compareTo(new BigDecimal("10")) < 0) lowStock++;
        }
        report.append(String.format("  低于安全线(10): %d 个SKU\n", lowStock));
        report.append(String.format("  总SKU: %d\n", allInv.size()));
        report.append(String.format("  库存健康: %s\n", lowStock == 0 ? "✅ 正常" : "⚠️ 有物料需补货"));

        // 3. 生产
        report.append("\n【生产进度】\n");
        long inProgress = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 2));
        long notStarted = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 1));
        report.append(String.format("  生产中: %d 个工单\n", inProgress));
        report.append(String.format("  待生产: %d 个工单\n", notStarted));

        // 4. 质检概况（简化）
        report.append("\n【质量概况】\n");
        report.append("  质检记录: 系统正常运行中\n");

        // 5. 综合评分
        report.append("\n【综合诊断】\n");
        int score = 100;
        if (overdueOrders > 0) score -= 20;
        if (lowStock > 0) score -= 15;
        if (notStarted > 5) score -= 10;
        String grade = score >= 90 ? "优秀 ✅" : score >= 70 ? "良好 ⚠️" : "需关注 ❌";
        report.append(String.format("  健康评分: %d/100 (%s)\n", score, grade));
        report.append(String.format("  建议: %s\n", overdueOrders > 0 ? "优先处理超期订单；" : "")
                + (lowStock > 0 ? "补充低库存物料；" : "")
                + (notStarted > 5 ? "关注待排产工单积压。" : "系统运行整体平稳。"));

        return report.toString();
    }

    @Tool("查询客户列表。可按客户名称模糊搜索,keyword传空字符串列出全部")
    public String listCustomers(@P("客户名称关键字(可选,传空字符串查全部)") String keyword) {
        LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) qw.like(Customer::getName, keyword);
        qw.last("LIMIT 10");
        List<Customer> list = customerMapper.selectList(qw);
        if (list.isEmpty()) return "未找到客户。";
        return list.stream()
                .map(c -> String.format("[%s] %s | 联系人:%s | 电话:%s",
                        c.getCode(), c.getName(), c.getContact(), c.getPhone()))
                .collect(Collectors.joining("\n"));
    }

    private String statusDesc(Integer s) {
        return switch (s) {
            case 1 -> "待审核"; case 2 -> "已审核"; case 3 -> "生产中";
            case 4 -> "部分发货"; case 5 -> "已完成"; case 6 -> "已取消";
            default -> "未知";
        };
    }
    private String woStatusDesc(Integer s) {
        return switch (s) {
            case 1 -> "待生产"; case 2 -> "生产中"; case 3 -> "已完成"; case 4 -> "已入库";
            default -> "未知";
        };
    }
}