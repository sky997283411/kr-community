package com.kr.community.config;

import com.baomidou.kisso.web.interceptor.SSOSpringInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 单点登录kisso 拦截器配置（基于cookie）
        registry.addInterceptor(new SSOSpringInterceptor()).addPathPatterns("/user/**").excludePathPatterns("/login");
    }
}
