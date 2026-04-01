package com.smartparking.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.userservice.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据微信 OpenID 查询用户
     * 
     * @param wechatOpenid 微信 OpenID
     * @return 用户信息
     */
    User findByWechatOpenid(@Param("wechatOpenid") String wechatOpenid);
}
