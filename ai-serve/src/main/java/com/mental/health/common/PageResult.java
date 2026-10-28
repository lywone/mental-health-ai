package com.mental.health.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 统一分页响应 {list, total, page, pageSize}
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private long total;
    private long page;
    private long pageSize;

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
