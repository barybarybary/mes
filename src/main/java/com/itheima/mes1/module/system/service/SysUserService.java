package com.itheima.mes1.module.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.mes1.module.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser login(String username, String password);
    Page<SysUser> pageUsers(int page, int pageSize, String keyword);
    void assignRoles(Long userId, Long[] roleIds);
}
