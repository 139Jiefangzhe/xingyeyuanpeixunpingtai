package com.playedu.train;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.playedu")
@MapperScan("com.playedu.train.mapper")
@EnableFeignClients(basePackages = "com.playedu.train.feign")
public class EduTrainApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduTrainApplication.class, args);
    }
}
