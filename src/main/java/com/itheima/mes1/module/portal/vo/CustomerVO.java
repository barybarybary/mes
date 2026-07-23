package com.itheima.mes1.module.portal.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerVO {
    private Long id;
    private String username;
    private String companyName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createTime;
}
