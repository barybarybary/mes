package com.itheima.mes1.module.system.vo;

import lombok.Data;
import java.util.List;

@Data
public class LoginVO {
    private String token;
    private SysUserVO user;
    private List<SysRoleVO> roles;
    private List<SysMenuVO> menus;
    private List<String> permissions;
}
