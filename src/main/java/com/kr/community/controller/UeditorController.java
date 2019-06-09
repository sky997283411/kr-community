package com.kr.community.controller;

import com.alibaba.fastjson.JSONException;
import com.kr.community.common.ueditor.ActionEnter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * 用于处理关于ueditor插件相关的请求
 */
@RestController
@RequestMapping("/ueditor")
@Slf4j
public class UeditorController {

	@RequestMapping(value = "/exec" )
	public String exec(HttpServletRequest request) throws UnsupportedEncodingException{
		request.setCharacterEncoding("utf-8");
		String rootPath = request.getRealPath("/");
		try {
			return new ActionEnter( request, rootPath ).exec();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}




	
}
