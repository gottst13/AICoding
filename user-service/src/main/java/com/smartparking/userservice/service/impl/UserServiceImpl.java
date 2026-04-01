package com.smartparking.userservice.service.impl;

import com.smartparking.userservice.dto.LoginRequest;
import com.smartparking.userservice.dto.LoginResponse;
import com.smartparking.userservice.entity.User;
import com.smartparking.userservice.mapper.UserMapper;
import com.smartparking.userservice.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Value("${app.jwt.secret:smart-parking-jwt-secret-key-2026}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:7200000}")
    private Long jwtExpiration;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录：username={}", request.getUsername());

        // 1. 查询用户
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 验证用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 3. TODO: 验证密码 (实际应该比对密码哈希)
        // String encodedPassword = passwordEncoder.encode(request.getPassword());
        // if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        //     throw new RuntimeException("用户名或密码错误");
        // }

        // 4. 生成 JWT Token
        String token = generateJwtToken(user);

        // 5. 构建响应
        return LoginResponse.builder()
                .token(token)
                .expiresIn(jwtExpiration / 1000)
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }

    @Override
    public void logout(String token) {
        log.info("用户登出：token={}", token);
        // TODO: 可以将 token 加入黑名单
    }

    /**
     * 生成 JWT Token
     */
    private String generateJwtToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}
