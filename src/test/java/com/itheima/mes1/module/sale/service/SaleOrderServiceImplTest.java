package com.itheima.mes1.module.sale.service;

import com.itheima.mes1.common.exception.BusinessException;
import com.itheima.mes1.module.sale.dto.SaleOrderCreateReq;
import com.itheima.mes1.module.sale.dto.SaleOrderItemReq;
import com.itheima.mes1.module.sale.dto.SaleOrderUpdateReq;
import com.itheima.mes1.module.sale.entity.SaleOrder;
import com.itheima.mes1.module.sale.entity.SaleOrderItem;
import com.itheima.mes1.module.sale.mapper.SaleOrderItemMapper;
import com.itheima.mes1.module.sale.mapper.SaleOrderMapper;
import com.itheima.mes1.module.sale.vo.SaleOrderVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleOrderServiceImplTest {

    @Mock private SaleOrderMapper orderMapper;
    @Mock private SaleOrderItemMapper itemMapper;

    @InjectMocks
    private SaleOrderService orderService;

    // ==================== create ====================

    @Test
    void create_shouldGenerateOrderNoAndSave() {
        SaleOrderCreateReq req = new SaleOrderCreateReq();
        req.setCustomerId(1L);
        req.setOrderDate(LocalDate.now());
        req.setItems(List.of(itemReq(10L, 2, 100)));

        // insert 时设置 ID，避免 getDetail 传 null
        doAnswer(inv -> {
            SaleOrder o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        }).when(orderMapper).insert(any(SaleOrder.class));
        doReturn(1).when(itemMapper).insert(any(SaleOrderItem.class));

        // getDetail 会调用 selectWithCustomer + selectByOrderId
        SaleOrder detail = new SaleOrder();
        detail.setId(1L);
        detail.setCustomerId(1L);
        detail.setCustomerName("测试客户");
        detail.setTotalAmount(new BigDecimal("200"));
        when(orderMapper.selectWithCustomer(1L)).thenReturn(detail);
        when(itemMapper.selectByOrderId(1L)).thenReturn(List.of());

        SaleOrderVO result = orderService.create(req);

        assertNotNull(result);
        assertEquals("测试客户", result.getCustomerName());

        ArgumentCaptor<SaleOrder> captor = ArgumentCaptor.forClass(SaleOrder.class);
        verify(orderMapper, atLeastOnce()).insert(captor.capture());
        SaleOrder saved = captor.getValue();
        assertTrue(saved.getOrderNo().startsWith("SO"));
        assertEquals(1, saved.getStatus());
    }

    @Test
    void create_shouldCalculateTotalAmount() {
        SaleOrderCreateReq req = new SaleOrderCreateReq();
        req.setCustomerId(1L);
        req.setOrderDate(LocalDate.now());
        req.setItems(List.of(
                itemReq(1L, 3, 50),
                itemReq(2L, 2, 75)
        ));

        doAnswer(inv -> {
            SaleOrder o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        }).when(orderMapper).insert(any(SaleOrder.class));
        doReturn(1).when(itemMapper).insert(any(SaleOrderItem.class));
        when(itemMapper.selectByOrderId(1L)).thenReturn(List.of());

        SaleOrder detail = new SaleOrder();
        detail.setId(1L);
        detail.setTotalAmount(new BigDecimal("300.00"));
        when(orderMapper.selectWithCustomer(1L)).thenReturn(detail);

        orderService.create(req);

        ArgumentCaptor<SaleOrder> captor = ArgumentCaptor.forClass(SaleOrder.class);
        verify(orderMapper, atLeastOnce()).updateById(captor.capture());
        assertEquals(0, new BigDecimal("300.00").compareTo(captor.getValue().getTotalAmount()));
    }

    // ==================== update ====================

    @Test
    void update_shouldThrow_whenStatusNotPending() {
        SaleOrder exist = new SaleOrder();
        exist.setId(1L);
        exist.setStatus(2);
        when(orderMapper.selectById(1L)).thenReturn(exist);

        SaleOrderUpdateReq req = new SaleOrderUpdateReq();
        req.setId(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.update(req));
        assertEquals("只有待审核的订单可以修改", ex.getMessage());
    }

    @Test
    void update_shouldAllowUpdate_whenStatusPending() {
        SaleOrder exist = new SaleOrder();
        exist.setId(1L);
        exist.setStatus(1);
        when(orderMapper.selectById(1L)).thenReturn(exist);

        SaleOrderUpdateReq req = new SaleOrderUpdateReq();
        req.setId(1L);
        req.setDeliveryDate(LocalDate.now().plusDays(3));

        assertDoesNotThrow(() -> orderService.update(req));
        verify(orderMapper).updateById(any(SaleOrder.class));
    }

    // ==================== delete ====================

    @Test
    void delete_shouldRemoveItemsAndOrder() {
        orderService.delete(1L);

        verify(itemMapper).deleteByOrderId(1L);
        verify(orderMapper).deleteById(1L);
    }

    // ==================== updateStatus ====================

    @Test
    void updateStatus_shouldCallMapper() {
        orderService.updateStatus(1L, 2);
        verify(orderMapper).updateStatus(1L, 2);
    }

    // ==================== helper ====================

    private SaleOrderItemReq itemReq(Long productId, int qty, int price) {
        SaleOrderItemReq item = new SaleOrderItemReq();
        item.setProductId(productId);
        item.setQuantity(BigDecimal.valueOf(qty));
        item.setPrice(BigDecimal.valueOf(price));
        item.setUnit("pcs");
        return item;
    }
}
