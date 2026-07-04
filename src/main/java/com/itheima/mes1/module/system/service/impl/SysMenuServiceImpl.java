package com.itheima.mes1.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.module.system.entity.SysMenu;
import com.itheima.mes1.module.system.mapper.SysMenuMapper;
import com.itheima.mes1.module.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> listTree() {
        List<SysMenu> all = menuMapper.selectList(null);
        return buildTree(all);
    }

    @Override
    public List<SysMenu> listByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectByUserId(userId);
        return buildTree(menus);
    }

    private List<SysMenu> buildTree(List<SysMenu> list) {
        return list.stream()
                .filter(m -> m.getParentId() == 0)
                .sorted(Comparator.comparingInt(SysMenu::getSort))
                .map(m -> { m.setChildren(getChildren(m, list)); return m; })
                .collect(Collectors.toList());
    }

    private List<SysMenu> getChildren(SysMenu parent, List<SysMenu> all) {
        return all.stream()
                .filter(m -> m.getParentId().equals(parent.getId()))
                .sorted(Comparator.comparingInt(SysMenu::getSort))
                .map(m -> { m.setChildren(getChildren(m, all)); return m; })
                .collect(Collectors.toList());
    }
}
