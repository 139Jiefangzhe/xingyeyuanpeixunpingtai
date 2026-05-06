package com.playedu.common.domain.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class PageResult<T> {
    private List<T> list;

    private long total;

    private long pageNum;

    private long pageSize;

    private long pages;

    public static <T, R> PageResult<R> fromPage(IPage<T> page, Function<T, R> mapper) {
        PageResult<R> result = new PageResult<>();
        List<T> records = page.getRecords() == null ? Collections.emptyList() : page.getRecords();
        result.setList(records.stream().map(mapper).collect(Collectors.toList()));
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}
