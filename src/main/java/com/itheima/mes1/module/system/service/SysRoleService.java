package com.itheima.mes1.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.mes1.module.system.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {
    void assignMenus(Long roleId, Long[] menuIds);
}
