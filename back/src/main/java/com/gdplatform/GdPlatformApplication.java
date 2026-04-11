package com.gdplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.gdplatform.mapper")
@EnableAsync
public class GdPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GdPlatformApplication.class, args);
    }
}
