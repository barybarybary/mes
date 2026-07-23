package com.itheima.mes1.module.system.vo;

import com.itheima.mes1.common.PageResult;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * 用户分页结果 — 在通用分页基础上附加用户统计字段
 */
@Getter
@Setter
public class UserPageResult<T> extends PageResult<T> {
    private long adminCount;
    private long enabledCount;
    private long disabledCount;

    public UserPageResult(List<T> list, long total, long page, long pageSize) {
        super(list, total, page, pageSize);
    }
}
