package com.smartparking.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单服务启动类
 */
@SpringBootApplication
@MapperScan("com.smartparking.order.mapper")
@ComponentScan(basePackages = {
    "com.smartparking.order",
    "com.smartparking.common"
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("   订单服务启动成功！");
        System.out.println("   访问地址：http://localhost:8081");
        System.out.println("   Swagger: http://localhost:8081/swagger-ui.html");
        System.out.println("=================================");
    }
}
