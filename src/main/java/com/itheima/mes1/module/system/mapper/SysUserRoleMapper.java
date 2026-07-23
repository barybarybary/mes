package com.itheima.mes1.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);

    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    void deleteByRoleId(Long roleId);

    /** 统计拥有 admin 角色的用户数（含关键字过滤） */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT ur.user_id) FROM sys_user_role ur " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "INNER JOIN sys_user u ON ur.user_id = u.id " +
            "WHERE r.code = 'admin' AND u.deleted = 0" +
            "<if test='keyword != null and keyword != \"\"'> AND u.username LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "</script>")
    long countAdminUsers(@Param("keyword") String keyword);
}
