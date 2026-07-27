package com.itheima.mes1.module.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.module.inventory.entity.Inventory;
import com.itheima.mes1.module.inventory.entity.InventoryTransaction;
import com.itheima.mes1.module.inventory.mapper.InventoryMapper;
import com.itheima.mes1.module.inventory.mapper.InventoryTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private InventoryTransactionMapper transactionMapper;
    @Autowired
    private com.itheima.mes1.module.inventory.mapper.StockAlertMapper stockAlertMapper;
    @Autowired
    private com.itheima.mes1.module.base.mapper.ProductMapper productMapper;
    @Autowired
    private com.itheima.mes1.module.base.mapper.WarehouseMapper warehouseMapper;
    @Autowired
    private com.itheima.mes1.module.system.mapper.SysUserMapper sysUserMapper;

    public List<Inventory> listAll() {
        return inventoryMapper.selectAllWithDetail();
    }

    public List<Inventory> listByProduct(Long productId) {
        return inventoryMapper.selectByProduct(productId);
    }

    public Page<Inventory> pageStocks(int page, int pageSize, Long productId) {
        int offset = (page - 1) * pageSize;
        List<Inventory> list = inventoryMapper.selectPageWithDetail(offset, pageSize, productId);
        long total = inventoryMapper.countStocks(productId);
        Page<Inventory> result = new Page<>(page, pageSize);
        result.setRecords(list);
        result.setTotal(total);
        return result;
    }

    public Page<InventoryTransaction> pageTransactions(int page, int pageSize, Long productId) {
        LambdaQueryWrapper<InventoryTransaction> w = new LambdaQueryWrapper<InventoryTransaction>()
                .eq(productId != null, InventoryTransaction::getProductId, productId)
                .orderByDesc(InventoryTransaction::getCreateTime);
        Page<InventoryTransaction> result = transactionMapper.selectPage(new Page<>(page, pageSize), w);
        result.setTotal(transactionMapper.selectCount(w));

        // 批量填充产品名、仓库名、操作人
        List<InventoryTransaction> records = result.getRecords();
        if (!records.isEmpty()) {
            // 产品名
            List<Long> productIds = records.stream().map(InventoryTransaction::getProductId).filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
            java.util.Map<Long, String> productNames = new java.util.HashMap<>();
            if (!productIds.isEmpty()) {
                productMapper.selectBatchIds(productIds).forEach(p -> productNames.put(p.getId(), p.getName()));
            }
            // 仓库名
            List<Long> warehouseIds = records.stream().map(InventoryTransaction::getWarehouseId).filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
            java.util.Map<Long, String> warehouseNames = new java.util.HashMap<>();
            if (!warehouseIds.isEmpty()) {
                warehouseMapper.selectBatchIds(warehouseIds).forEach(wh -> warehouseNames.put(wh.getId(), wh.getName()));
            }
            // 操作人
            List<Long> userIds = records.stream().map(InventoryTransaction::getCreateBy).filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
            java.util.Map<Long, String> userNicknames = new java.util.HashMap<>();
            if (!userIds.isEmpty()) {
                sysUserMapper.selectBatchIds(userIds).forEach(u -> userNicknames.put(u.getId(), u.getNickname() != null ? u.getNickname() : u.getUsername()));
            }
            records.forEach(r -> {
                r.setProductName(productNames.get(r.getProductId()));
                r.setWarehouseName(warehouseNames.get(r.getWarehouseId()));
                if (r.getCreateBy() != null) r.setOperator(userNicknames.get(r.getCreateBy()));
            });
        }

        return result;
    }

    /**
     * 入库: productId, warehouseId, locationId, batchNo, quantity, type, orderNo
     */
    @Transactional
    public void stockIn(Long productId, Long warehouseId, Long locationId,
                        String batchNo, BigDecimal quantity, String type, String orderNo, String remark) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("入库数量必须大于0");

        // 查找已有库存记录
        Inventory inventory = findOrCreateInventory(productId, warehouseId, locationId, batchNo);
        BigDecimal before = inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO;
        inventory.setQuantity(before.add(quantity));
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(inventory);

        // 记录流水（使用实际库存记录的 warehouseId，确保 NOT NULL 列有值）
        saveTransaction(productId,
                inventory.getWarehouseId() != null ? inventory.getWarehouseId() : warehouseId,
                batchNo, type, quantity, before, inventory.getQuantity(), orderNo, remark);
    }

    /**
     * 出库
     */
    @Transactional
    public void stockOut(Long productId, Long warehouseId, String batchNo,
                         BigDecimal quantity, String type, String orderNo, String remark) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("出库数量必须大于0");

        // 查库存 (出库不指定库位,按批次扣)
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getProductId, productId)
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId);
        if (batchNo != null) w.eq(Inventory::getBatchNo, batchNo);

        List<Inventory> list = inventoryMapper.selectList(w);
        BigDecimal totalStock = list.stream()
                .map(i -> i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalStock.compareTo(quantity) < 0) throw new BusinessException("库存不足");

        // 先进先出扣减
        BigDecimal remain = quantity;
        for (Inventory inv : list) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal invQty = inv.getQuantity() != null ? inv.getQuantity() : BigDecimal.ZERO;
            BigDecimal deduct = invQty.compareTo(remain) <= 0 ? invQty : remain;
            BigDecimal before = invQty;
            inv.setQuantity(invQty.subtract(deduct));
            inv.setUpdateTime(LocalDateTime.now());
            inventoryMapper.updateById(inv);
            // 使用实际库存记录的 warehouseId，确保 NOT NULL 列有值
            saveTransaction(productId,
                    inv.getWarehouseId() != null ? inv.getWarehouseId() : warehouseId,
                    inv.getBatchNo(), type,
                    deduct.negate(), before, inv.getQuantity(), orderNo, remark);
            remain = remain.subtract(deduct);
        }

        // 出库后检查库存预警
        checkAndAlert(productId);
    }

    /**
     * 调拨: 从源仓库出库 → 目标仓库入库
     */
    @Transactional
    public void transfer(Long productId, Long fromWarehouseId, Long toWarehouseId,
                         String batchNo, BigDecimal quantity, String remark) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("调拨数量必须大于0");
        if (fromWarehouseId.equals(toWarehouseId)) throw new BusinessException("源仓库和目标仓库不能相同");

        String orderNo = "TRF-" + System.currentTimeMillis();

        // 从源仓库出库
        stockOut(productId, fromWarehouseId, batchNo, quantity, "transfer", orderNo, remark);

        // 目标仓库入库(不指定库位)
        stockIn(productId, toWarehouseId, null, batchNo, quantity, "transfer", orderNo, remark);
    }

    /** 检查单个产品库存是否低于阈值，如低于且无未处理预警则生成预警 */
    private void checkAndAlert(Long productId) {
        BigDecimal threshold = new BigDecimal("10");
        List<Inventory> allInv = inventoryMapper.selectByProduct(productId);
        BigDecimal totalQty = allInv.stream()
                .map(i -> i.getQuantity() != null ? i.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalQty.compareTo(threshold) <= 0) {
            // 检查是否已有未处理预警
            long count = stockAlertMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.itheima.mes1.module.inventory.entity.StockAlert>()
                            .eq(com.itheima.mes1.module.inventory.entity.StockAlert::getProductId, productId)
                            .eq(com.itheima.mes1.module.inventory.entity.StockAlert::getStatus, 0));
            if (count == 0) {
                com.itheima.mes1.module.inventory.entity.StockAlert alert = new com.itheima.mes1.module.inventory.entity.StockAlert();
                alert.setProductId(productId);
                alert.setCurrentQty(totalQty);
                alert.setThresholdQty(threshold);
                alert.setStatus(0);
                var p = productMapper.selectById(productId);
                if (p != null) alert.setProductName(p.getName());
                stockAlertMapper.insert(alert);
            }
        }
    }

    private Inventory findOrCreateInventory(Long productId, Long warehouseId, Long locationId, String batchNo) {
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getProductId, productId)
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId);
        if (batchNo != null) w.eq(Inventory::getBatchNo, batchNo);

        Inventory inv = inventoryMapper.selectOne(w);
        if (inv == null) {
            inv = new Inventory();
            inv.setProductId(productId);
            inv.setWarehouseId(warehouseId);
            inv.setLocationId(locationId);
            inv.setBatchNo(batchNo);
            inv.setQuantity(BigDecimal.ZERO);
            inv.setLockedQty(BigDecimal.ZERO);
            inv.setUpdateTime(LocalDateTime.now());
            inventoryMapper.insert(inv);
        }
        return inv;
    }

    private void saveTransaction(Long productId, Long warehouseId, String batchNo,
                                  String type, BigDecimal quantity, BigDecimal before, BigDecimal after,
                                  String orderNo, String remark) {
        InventoryTransaction t = new InventoryTransaction();
        t.setProductId(productId);
        t.setWarehouseId(warehouseId);
        t.setBatchNo(batchNo);
        t.setType(type);
        t.setQuantity(quantity);
        t.setBeforeQty(before);
        t.setAfterQty(after);
        t.setOrderNo(orderNo);
        t.setRemark(remark);
        t.setCreateTime(LocalDateTime.now());
        transactionMapper.insert(t);
    }
}
