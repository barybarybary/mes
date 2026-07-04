package com.itheima.mes1.module.bi.service;

import com.itheima.mes1.module.bi.mapper.BiMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BiPivotService {

    @Autowired
    private BiMapper biMapper;

    public List<Map<String, Object>> salesByProduct(Integer year, Integer month) {
        return biMapper.pivotSalesByProduct(year, month);
    }

    public List<Map<String, Object>> salesByCustomer(Integer year, Integer month) {
        return biMapper.pivotSalesByCustomer(year, month);
    }

    public List<Map<String, Object>> salesByMonthCategory(Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        LocalDateTime start = LocalDate.of(y, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(y + 1, 1, 1).atStartOfDay();
        return biMapper.pivotSalesByMonthCategory(start, end);
    }

    public List<Map<String, Object>> inventoryByWarehouse() {
        return biMapper.pivotInventoryByWarehouse();
    }

    public List<Map<String, Object>> productionByMonth(Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        LocalDateTime start = LocalDate.of(y, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(y + 1, 1, 1).atStartOfDay();
        return biMapper.pivotProductionByMonth(start, end);
    }

    public List<Map<String, Object>> deliveryByCustomer(Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        LocalDate start = LocalDate.of(y, 1, 1);
        LocalDate end = LocalDate.of(y + 1, 1, 1);
        return biMapper.pivotDeliveryByCustomer(start, end);
    }
}