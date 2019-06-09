package com.kr.community.config.datasources.aspect;

import com.kr.community.config.datasources.DataSourceNames;
import com.kr.community.config.datasources.DynamicDataSource;
import com.kr.community.config.datasources.annotation.DataSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 多数据源，切面处理类,读写分离
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2017/9/16 22:20
 */
@Aspect
@Component
public class DataSourceAspect implements Ordered {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    String[] writeSourcePrefix = {"insert","update","save"};

    @Pointcut("@annotation(com.kr.community.config.datasources.annotation.DataSource)")
    public void dataSourcePointCut() {

    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        DataSource ds = method.getAnnotation(DataSource.class);
        if(ds == null){
            boolean isWriteSource = false;
            for(int i=0; i<writeSourcePrefix.length; i++ ){
                if(methodName.toLowerCase().contains(writeSourcePrefix[i])){
                    DynamicDataSource.setDataSource(DataSourceNames.FIRST);
                    isWriteSource =true;
                    break;
                }
            }
            if(!isWriteSource){
                DynamicDataSource.setDataSource(DataSourceNames.SECOND);
            }
            logger.debug("set datasource is " + DataSourceNames.FIRST);
        }else {
            DynamicDataSource.setDataSource(ds.name());
            logger.debug("set datasource is " + ds.name());
        }

        try {
            return point.proceed();
        } finally {
            DynamicDataSource.clearDataSource();
            logger.debug("clean datasource");
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
