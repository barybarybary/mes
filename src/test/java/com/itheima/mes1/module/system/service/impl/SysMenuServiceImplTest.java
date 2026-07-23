package com.itheima.mes1.module.system.service.impl;

import com.itheima.mes1.module.system.SysConverter;
import com.itheima.mes1.module.system.entity.SysMenu;
import com.itheima.mes1.module.system.vo.SysMenuVO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜单树构建单元测试 — 覆盖 parent_id=null 的 NPE 修复
 */
class SysMenuServiceImplTest {

    @Test
    void testBuildTreeWithNullParentId() {
        List<SysMenu> flat = new ArrayList<>();
        flat.add(menu(1L, null, "仪表盘", 1, "/dashboard", 1));
        flat.add(menu(2L, 1L, "首页概览", 2, "/dashboard", 1));
        flat.add(menu(10L, null, "系统管理", 1, "/system", 10));
        flat.add(menu(11L, 10L, "用户管理", 2, "/system/user", 11));
        flat.add(menu(12L, 10L, "角色管理", 2, "/system/role", 12));

        List<SysMenuVO> tree = SysConverter.buildMenuTree(flat);

        assertEquals(2, tree.size());
        assertEquals("仪表盘", tree.get(0).getName());
        assertEquals("系统管理", tree.get(1).getName());

        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("首页概览", tree.get(0).getChildren().get(0).getName());

        assertEquals(2, tree.get(1).getChildren().size());
    }

    @Test
    void testBuildTreeWithZeroParentId() {
        List<SysMenu> flat = new ArrayList<>();
        flat.add(menu(1L, 0L, "首页", 2, "/dashboard", 1));
        flat.add(menu(2L, 0L, "系统管理", 1, "/system", 10));
        flat.add(menu(3L, 2L, "用户管理", 2, "/system/user", 11));

        List<SysMenuVO> tree = SysConverter.buildMenuTree(flat);

        assertEquals(2, tree.size());
        assertEquals("首页", tree.get(0).getName());
        assertEquals("系统管理", tree.get(1).getName());
        assertEquals(1, tree.get(1).getChildren().size());
    }

    @Test
    void testBuildTreeEmptyList() {
        List<SysMenuVO> tree = SysConverter.buildMenuTree(new ArrayList<>());
        assertTrue(tree.isEmpty());
    }

    @Test
    void testBuildTreeAllRoot() {
        List<SysMenu> flat = new ArrayList<>();
        flat.add(menu(1L, null, "A", 2, "/a", 1));
        flat.add(menu(2L, null, "B", 2, "/b", 2));
        flat.add(menu(3L, null, "C", 2, "/c", 3));

        List<SysMenuVO> tree = SysConverter.buildMenuTree(flat);

        assertEquals(3, tree.size());
        tree.forEach(m -> assertTrue(m.getChildren().isEmpty()));
    }

    @Test
    void testBuildTreeDeepNesting() {
        List<SysMenu> flat = new ArrayList<>();
        flat.add(menu(1L, null, "一级", 1, "/l1", 1));
        flat.add(menu(2L, 1L, "二级", 1, "/l2", 1));
        flat.add(menu(3L, 2L, "三级", 2, "/l3", 1));

        List<SysMenuVO> tree = SysConverter.buildMenuTree(flat);

        assertEquals(1, tree.size());
        assertEquals("一级", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("二级", tree.get(0).getChildren().get(0).getName());
        assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size());
        assertEquals("三级", tree.get(0).getChildren().get(0).getChildren().get(0).getName());
    }

    @Test
    void testBuildTreeSortOrder() {
        List<SysMenu> flat = new ArrayList<>();
        flat.add(menu(3L, null, "C", 2, "/c", 30));
        flat.add(menu(1L, null, "A", 2, "/a", 10));
        flat.add(menu(2L, null, "B", 2, "/b", 20));

        List<SysMenuVO> tree = SysConverter.buildMenuTree(flat);

        assertEquals("A", tree.get(0).getName());
        assertEquals("B", tree.get(1).getName());
        assertEquals("C", tree.get(2).getName());
    }

    private SysMenu menu(Long id, Long parentId, String name, Integer type, String path, Integer sort) {
        SysMenu m = new SysMenu();
        m.setId(id);
        m.setParentId(parentId);
        m.setName(name);
        m.setType(type);
        m.setPath(path);
        m.setSort(sort);
        m.setVisible(1);
        return m;
    }
}
