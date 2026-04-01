package com.smartparking.parking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 停车服务启动类
 */
@SpringBootApplication
@MapperScan("com.smartparking.parking.mapper")
@ComponentScan(basePackages = {
    "com.smartparking.parking",
    "com.smartparking.common"
})
public class ParkingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("   停车服务启动成功！");
        System.out.println("   访问地址：http://localhost:8080");
        System.out.println("   Swagger: http://localhost:8080/swagger-ui.html");
        System.out.println("=================================");
    }
}
