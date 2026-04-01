package com.smartparking.userservice.service;

import com.smartparking.userservice.dto.LoginRequest;
import com.smartparking.userservice.dto.LoginResponse;

/**
 * 用户服务接口
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     * 
     * @param token JWT Token
     */
    void logout(String token);
}
