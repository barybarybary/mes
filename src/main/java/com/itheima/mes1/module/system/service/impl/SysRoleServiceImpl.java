package com.itheima.mes1.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.module.system.entity.SysRole;
import com.itheima.mes1.module.system.entity.SysRoleMenu;
import com.itheima.mes1.module.system.mapper.SysRoleMapper;
import com.itheima.mes1.module.system.mapper.SysRoleMenuMapper;
import com.itheima.mes1.module.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Override
    @Transactional
    public void assignMenus(Long roleId, Long[] menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
