package com.itheima.mes1.module.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mes1.module.system.SysConverter;
import com.itheima.mes1.module.system.entity.SysMenu;
import com.itheima.mes1.module.system.mapper.SysMenuMapper;
import com.itheima.mes1.module.system.service.SysMenuService;
import com.itheima.mes1.module.system.vo.SysMenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public List<SysMenuVO> listTree() {
        List<SysMenu> all = menuMapper.selectList(null);
        return SysConverter.buildMenuTree(all);
    }

    @Override
    public List<SysMenuVO> listTreeByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectByUserId(userId);
        return SysConverter.buildMenuTree(menus);
    }
}
