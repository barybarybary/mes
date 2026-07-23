package com.itheima.mes1.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.mes1.module.system.entity.SysMenu;
import com.itheima.mes1.module.system.vo.SysMenuVO;
import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    List<SysMenuVO> listTree();
    List<SysMenuVO> listTreeByUserId(Long userId);
}
