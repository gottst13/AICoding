package com.smartparking.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停车场实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("parking_lots")
public class ParkingLot {

    /**
     * 停车场 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 停车场名称
     */
    private String name;

    /**
     * 停车场编码
     */
    private String code;

    /**
     * 地址
     */
    private String address;

    /**
     * 总车位数
     */
    private Integer totalSpaces;

    /**
     * 类型：1-封闭车场 2-路侧泊位
     */
    private Integer type;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 配置信息 (JSON)
     */
    private String config;

    /**
     * 二维码 URL
     */
    private String qrCodeUrl;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
