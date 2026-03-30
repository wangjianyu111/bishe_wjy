package com.gdplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gdplatform.mapper")
public class GdPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GdPlatformApplication.class, args);
    }
}
