package com.smartparking.userservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.smartparking.userservice.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("   用户服务启动成功！");
        System.out.println("   端口：8081");
        System.out.println("   Swagger: http://localhost:8081/swagger-ui.html");
        System.out.println("=================================");
    }
}
