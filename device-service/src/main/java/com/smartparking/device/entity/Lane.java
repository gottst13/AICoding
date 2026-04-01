package com.smartparking.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车道实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("lanes")
public class Lane {

    /**
     * 车道 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 停车场 ID
     */
    private Long parkingLotId;

    /**
     * 车道名称
     */
    private String name;

    /**
     * 车道编码
     */
    private String code;

    /**
     * 车道类型：1-入口 2-出口 3-双向
     */
    private Integer laneType;

    /**
     * 关联区域 ID
     */
    private Long zoneId;

    /**
     * 设备 IP 地址
     */
    private String deviceIp;

    /**
     * 设备端口
     */
    private Integer devicePort;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

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
