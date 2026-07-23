package com.itheima.mes1.module.production;

import com.itheima.mes1.module.production.entity.*;
import com.itheima.mes1.module.production.vo.*;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ProductionConverter {

    public static WorkOrderVO toVO(WorkOrder wo) {
        if (wo == null) return null;
        WorkOrderVO vo = new WorkOrderVO();
        BeanUtils.copyProperties(wo, vo, "processes");
        if (wo.getProcesses() != null) {
            vo.setProcesses(wo.getProcesses().stream()
                    .map(ProductionConverter::toVO)
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    public static WorkOrderProcessVO toVO(WorkOrderProcess wp) {
        if (wp == null) return null;
        WorkOrderProcessVO vo = new WorkOrderProcessVO();
        BeanUtils.copyProperties(wp, vo);
        return vo;
    }

    public static List<WorkOrderVO> toVOList(List<WorkOrder> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(ProductionConverter::toVO).collect(Collectors.toList());
    }
}
