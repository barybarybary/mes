package com.itheima.mes1.module.system.dto;

import lombok.Data;

@Data
public class LoginReq {
    private String username;
    private String password;
    private String captchaKey;
    private String captchaAnswer;
}
