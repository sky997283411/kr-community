/**
 * Copyright 2018 人人开源 http://www.renren.io
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kr.community.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * Map工具类
 *
 */
public class MapUtils extends HashMap<String, Object> {

    public static final String METHOD_PREFIX = "get";

    @Override
    public MapUtils put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * 将对象属性的值转化为map
     * @param obj
     * @return
     */
    public static Map<String, Object> transformTomap(Object obj){
        Map<String, Object> fieldsMap = new HashMap<>(20);
        Class<?> clazz = obj.getClass();
        //获取post.class里面声明的所有方法， getmethods时获取本类及父类声明的所有方法
        Method[] methods = clazz.getDeclaredMethods();
        Field[] fields = clazz.getDeclaredFields();
        for(Method method: methods){
            String methodName = method.getName().toLowerCase();
            if(methodName.startsWith(METHOD_PREFIX)){
                for(Field field: fields){
                    String fieldName = field.getName().toLowerCase();
                    if((METHOD_PREFIX + fieldName).equals(methodName)){
                        try {
                            fieldsMap.put(field.getName(),method.invoke(obj));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return fieldsMap;
    }
}
