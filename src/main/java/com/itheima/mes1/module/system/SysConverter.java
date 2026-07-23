package com.itheima.mes1.module.system;

import com.itheima.mes1.module.system.dto.*;
import com.itheima.mes1.module.system.entity.*;
import com.itheima.mes1.module.system.vo.*;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity ↔ DTO/VO 转换工具
 */
public class SysConverter {

    // ==================== SysUser ====================

    public static SysUser toEntity(SysUserCreateReq req) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(req, user);
        return user;
    }

    public static SysUser toEntity(SysUserUpdateReq req) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(req, user);
        return user;
    }

    public static SysUserVO toVO(SysUser user) {
        if (user == null) return null;
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    // ==================== SysRole ====================

    public static SysRole toEntity(SysRoleCreateReq req) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(req, role);
        return role;
    }

    public static SysRole toEntity(SysRoleUpdateReq req) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(req, role);
        return role;
    }

    public static SysRoleVO toVO(SysRole role) {
        if (role == null) return null;
        SysRoleVO vo = new SysRoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    public static List<SysRoleVO> toRoleVOList(List<SysRole> roles) {
        if (roles == null) return Collections.emptyList();
        return roles.stream().map(SysConverter::toVO).collect(Collectors.toList());
    }

    // ==================== SysMenu ====================

    public static SysMenuVO toVO(SysMenu menu) {
        if (menu == null) return null;
        SysMenuVO vo = new SysMenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }

    public static List<SysMenuVO> toMenuVOList(List<SysMenu> menus) {
        if (menus == null) return Collections.emptyList();
        return menus.stream().map(SysConverter::toVO).collect(Collectors.toList());
    }

    /** 构建菜单树（返回 VO） */
    public static List<SysMenuVO> buildMenuTree(List<SysMenu> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .sorted(Comparator.comparingInt(SysMenu::getSort))
                .map(m -> {
                    SysMenuVO vo = toVO(m);
                    vo.setChildren(getChildren(m, list));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private static List<SysMenuVO> getChildren(SysMenu parent, List<SysMenu> all) {
        return all.stream()
                .filter(m -> m.getParentId() != null && m.getParentId().equals(parent.getId()))
                .sorted(Comparator.comparingInt(SysMenu::getSort))
                .map(m -> {
                    SysMenuVO vo = toVO(m);
                    vo.setChildren(getChildren(m, all));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /** 提取菜单树中所有权限标识 */
    public static List<String> collectPermissions(List<SysMenuVO> menus) {
        List<String> result = new ArrayList<>();
        collectPerms(menus, result);
        return result;
    }

    private static void collectPerms(List<SysMenuVO> menus, List<String> result) {
        for (SysMenuVO m : menus) {
            if (m.getPermission() != null) {
                result.add(m.getPermission());
            }
            if (m.getChildren() != null) {
                collectPerms(m.getChildren(), result);
            }
        }
    }
}
