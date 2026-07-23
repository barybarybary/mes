package com.itheima.mes1.module.production.service;

import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.common.mq.MessageSender;
import com.itheima.mes1.module.production.entity.WorkOrder;
import com.itheima.mes1.module.production.mapper.WorkOrderMapper;
import com.itheima.mes1.module.production.mapper.WorkOrderProcessMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock private WorkOrderMapper workOrderMapper;
    @Mock private WorkOrderProcessMapper processMapper;
    @Mock private MessageSender messageSender;

    @InjectMocks
    private WorkOrderService workOrderService;

    // ==================== create ====================

    @Test
    void create_shouldGenerateOrderNoAndSetDefaults() {
        WorkOrder wo = new WorkOrder();
        wo.setProductId(1L);
        wo.setQuantity(new BigDecimal("100"));

        doReturn(1).when(workOrderMapper).insert(any(WorkOrder.class));

        WorkOrder result = workOrderService.create(wo);

        assertNotNull(result);
        assertTrue(result.getOrderNo().startsWith("WO"));
        assertEquals(1, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getFinishedQty());
        assertEquals(BigDecimal.ZERO, result.getQualifiedQty());
        assertEquals(BigDecimal.ZERO, result.getScrapQty());
    }

    // ==================== startWork ====================

    @Test
    void startWork_shouldSetStatusToInProgress() {
        WorkOrder wo = buildWorkOrder(1L, 1);
        when(workOrderMapper.selectWithProduct(1L)).thenReturn(wo);
        doReturn(1).when(workOrderMapper).updateById(any(WorkOrder.class));

        workOrderService.startWork(1L);

        assertEquals(2, wo.getStatus());
        assertNotNull(wo.getActualStart());
        verify(messageSender).sendEvent(contains("workorder.2"), anyMap());
    }

    @Test
    void startWork_shouldThrow_whenNotPending() {
        WorkOrder wo = buildWorkOrder(1L, 2);
        when(workOrderMapper.selectWithProduct(1L)).thenReturn(wo);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.startWork(1L));
        assertEquals("只有待生产的工单可以开工", ex.getMessage());
    }

    // ==================== complete ====================

    @Test
    void complete_shouldSetStatusToFinished() {
        WorkOrder wo = buildWorkOrder(1L, 2);
        when(workOrderMapper.selectWithProduct(1L)).thenReturn(wo);
        doReturn(1).when(workOrderMapper).updateById(any(WorkOrder.class));

        workOrderService.complete(1L);

        assertEquals(3, wo.getStatus());
        assertNotNull(wo.getActualEnd());
    }

    @Test
    void complete_shouldThrow_whenNotInProgress() {
        WorkOrder wo = buildWorkOrder(1L, 1);
        when(workOrderMapper.selectWithProduct(1L)).thenReturn(wo);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.complete(1L));
        assertEquals("只有生产中的工单可以完成", ex.getMessage());
    }

    // ==================== finishAndStockIn ====================

    @Test
    void finishAndStockIn_shouldSetStatusToStocked() {
        WorkOrder wo = buildWorkOrder(1L, 3);
        when(workOrderMapper.selectWithProduct(1L)).thenReturn(wo);
        doReturn(1).when(workOrderMapper).updateById(any(WorkOrder.class));

        workOrderService.finishAndStockIn(1L);

        assertEquals(4, wo.getStatus());
    }

    @Test
    void finishAndStockIn_shouldThrow_whenNotCompleted() {
        WorkOrder wo = buildWorkOrder(1L, 2);
        when(workOrderMapper.selectWithProduct(1L)).thenReturn(wo);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.finishAndStockIn(1L));
        assertEquals("只有已完成的工单可以入库", ex.getMessage());
    }

    // ==================== helper ====================

    private WorkOrder buildWorkOrder(Long id, int status) {
        WorkOrder wo = new WorkOrder();
        wo.setId(id);
        wo.setOrderNo("WO202607230001");
        wo.setProductId(1L);
        wo.setProductName("测试产品");
        wo.setStatus(status);
        wo.setQuantity(new BigDecimal("100"));
        return wo;
    }
}
