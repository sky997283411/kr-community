package com.kr.community.common.utils;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class RedisPage<T> implements Serializable {

    private List<T> records;
    private long total;
    private long pageSize;
    private long pageIndex;

    public RedisPage(){
        this.records = Collections.emptyList();
        this.total = 0L;
        this.pageSize = 10L;
        this.pageIndex = 1L;
    }

    public RedisPage(long pageIndex, long pageSize){
        this.records = Collections.emptyList();
        this.total = 0L;
        this.pageSize = 10L;
        this.pageIndex = 1L;
        if (pageIndex > 1L) {
            this.pageIndex = pageIndex;
        }
        this.pageSize = pageSize;
    }

    public boolean hasPrevious() {
        return this.pageIndex > 1L;
    }

    public boolean hasNext() {
        return this.pageIndex < this.getPages();
    }

    public List<T> getRecords() {
        return this.records;
    }

    private long getPages() {
        if (this.getPageSize() == 0L) {
            return 0L;
        } else {
            long pages = this.getTotal() / this.getPageSize();
            if (this.getTotal() % this.getPageSize() != 0L) {
                ++pages;
            }

            return pages;
        }
    }

}
