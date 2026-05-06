package com.playedu.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.playedu")
@MapperScan("com.playedu.live.mapper")
public class EduLiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduLiveApplication.class, args);
    }
}
