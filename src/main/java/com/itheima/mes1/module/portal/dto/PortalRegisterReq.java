package com.itheima.mes1.module.portal.dto;

import lombok.Data;

@Data
public class PortalRegisterReq {
    private String username;
    private String password;
    private String companyName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
}
