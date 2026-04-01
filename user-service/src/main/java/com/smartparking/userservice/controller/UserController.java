package com.smartparking.userservice.controller;

import com.smartparking.userservice.dto.LoginRequest;
import com.smartparking.userservice.dto.LoginResponse;
import com.smartparking.userservice.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Api(tags = "用户认证接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public LoginResponse login(@RequestBody @Validated LoginRequest request) {
        return userService.login(request);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public void logout(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        // 提取 Token (去掉 Bearer 前缀)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        userService.logout(token);
    }
}
