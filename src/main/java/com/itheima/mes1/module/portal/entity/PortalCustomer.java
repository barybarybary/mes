package com.itheima.mes1.module.portal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("portal_customer")
public class PortalCustomer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String companyName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
