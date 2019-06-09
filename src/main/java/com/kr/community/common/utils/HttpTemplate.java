package com.kr.community.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class HttpTemplate {

    public final static String QQ_LOGIN_URL="https://graph.qq.com/user/get_user_info";

    public static void main(String[] args) {
        JSONObject json = doGetqq("101585860","8A98E3D1B0BF062431BE99DD726E0B0C",
                "9900A1DE75CC9FEAC3A1B678B940A424");
        System.out.println(json.toJSONString());
    }

    public static JSONObject  doGetqq(String oauth_consumer_key,String access_token,String openid){
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity(headers);
        String url = QQ_LOGIN_URL + "?oauth_consumer_key=" + oauth_consumer_key + "&access_token=" + access_token
                + "&openid=" + openid;
        String result=  restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        return JSON.parseObject(result);
    }
}
