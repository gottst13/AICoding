package com.smartparking.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("devices")
public class Device {

    /**
     * 设备 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 停车场 ID
     */
    private Long parkingLotId;

    /**
     * 车道 ID
     */
    private Long laneId;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备类型：1-相机 2-道闸 3-地感 4-显示屏
     */
    private Integer deviceType;

    /**
     * 品牌型号
     */
    private String brandModel;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态：0-离线 1-在线 2-故障
     */
    private Integer status;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

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
