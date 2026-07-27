package com.itheima.mes1.module.portal.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderProgressItemVO {
    /** 步骤标题：已下单、已付款、生产中、已发货、已完成、已取消 */
    private String title;
    /** 步骤描述 */
    private String description;
    /** 发生时间 */
    private LocalDateTime time;
    /** 步骤状态：completed（已完成）、active（当前进行中）、pending（待进行） */
    private String status;
    /** 图标名称（前端渲染用） */
    private String icon;
}
