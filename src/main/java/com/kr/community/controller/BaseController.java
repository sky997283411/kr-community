package com.kr.community.controller;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kr.community.common.utils.RedisUtil;
import com.kr.community.service.CommentService;
import com.kr.community.service.PostNewService;
import com.kr.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class BaseController {

    @Autowired
    HttpServletRequest req;



    @Autowired
    PostNewService postNewService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ObjectMapper objectMapper;


}
