package com.kr.community.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kr.community.service.BaseService;

import java.util.List;
import java.util.Map;

public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

    @Override
    public void join(Map<String, Object> stringObjectMap, String field) {}

    @Override
    public void join(List<Map<String, Object>> datas, String field) {
        datas.forEach(map -> {
            this.join(map, field);
        });
    }

    @Override
    public void join(IPage<Map<String, Object>> pageData, String field) {
        List<Map<String, Object>> records = pageData.getRecords();
        this.join(records, field);
    }
}
