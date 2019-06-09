package com.kr.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class KrCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(KrCommunityApplication.class, args);
    }

}
