package com.smartparking.userservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求 DTO
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码 (可选)
     */
    private String captcha;
}
