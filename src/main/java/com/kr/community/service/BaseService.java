package com.kr.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface BaseService<T> extends IService<T> {

    /**
     * 添加关联表信息
     * @param stringObjectMap
     * @param field
     */
    void join(Map<String, Object> stringObjectMap, String field);

    void join(List<Map<String, Object>> datas, String field);

    void join(IPage<Map<String, Object>> pageData, String field);

}
