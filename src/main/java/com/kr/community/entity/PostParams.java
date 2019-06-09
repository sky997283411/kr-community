package com.kr.community.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostParams implements Serializable {

    private int  pageIndex;

    private int pageSize;

    private int categoryId;

    private int type;

    private int order;
}
