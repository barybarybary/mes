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

    public List<Inventory> listAll() {
        return inventoryMapper.selectAllWithDetail();
    }

    public List<Inventory> listByProduct(Long productId) {
        return inventoryMapper.selectByProduct(productId);
    }

    public Page<InventoryTransaction> pageTransactions(int page, int pageSize, Long productId) {
        LambdaQueryWrapper<InventoryTransaction> w = new LambdaQueryWrapper<InventoryTransaction>()
                .eq(productId != null, InventoryTransaction::getProductId, productId)
                .orderByDesc(InventoryTransaction::getCreateTime);
        Page<InventoryTransaction> result = transactionMapper.selectPage(new Page<>(page, pageSize), w);
        result.setTotal(transactionMapper.selectCount(w));
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

        // 记录流水
        saveTransaction(productId, warehouseId, batchNo, type, quantity, before, inventory.getQuantity(), orderNo, remark);
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
                .eq(Inventory::getWarehouseId, warehouseId);
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
            saveTransaction(productId, warehouseId, inv.getBatchNo(), type,
                    deduct.negate(), before, inv.getQuantity(), orderNo, remark);
            remain = remain.subtract(deduct);
        }
    }

    private Inventory findOrCreateInventory(Long productId, Long warehouseId, Long locationId, String batchNo) {
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getProductId, productId)
                .eq(Inventory::getWarehouseId, warehouseId);
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
