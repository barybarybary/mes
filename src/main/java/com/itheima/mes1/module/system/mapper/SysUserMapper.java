package com.itheima.mes1.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mes1.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 物理删除（绕过逻辑删除），用于注册前清理软删除残留 */
    @Delete("DELETE FROM sys_user WHERE username = #{username} OR email = #{email}")
    int physicalDelete(@Param("username") String username, @Param("email") String email);
}
