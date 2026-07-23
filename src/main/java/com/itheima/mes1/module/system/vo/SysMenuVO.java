package com.itheima.mes1.module.system.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysMenuVO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer type;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer sort;
    private Integer visible;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<SysMenuVO> children;
}
