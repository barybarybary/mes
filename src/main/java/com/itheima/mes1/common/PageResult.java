package com.itheima.mes1.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private long page;
    private long pageSize;
    /** 管理员人数（全局统计，不受分页影响） */
    private long adminCount;
    /** 启用用户数（全局统计，不受分页影响） */
    private long enabledCount;
    /** 禁用用户数（全局统计，不受分页影响） */
    private long disabledCount;

    public PageResult(List<T> list, long total, long page, long pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
