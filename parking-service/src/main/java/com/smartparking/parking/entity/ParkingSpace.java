package com.smartparking.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 车位实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@Builder
@TableName("parking_spaces")
public class ParkingSpace {

    /**
     * 车位 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域 ID
     */
    private Long zoneId;

    /**
     * 车位编号 (如：B1-A001)
     */
    private String spaceNo;

    /**
     * 车位类型：1-小型车 2-大型车 3-无障碍 4-充电车位
     */
    private Integer spaceType;

    /**
     * 状态：0-空闲 1-占用 2-锁定
     */
    private Integer status;

    /**
     * 宽度 (厘米)
     */
    private Integer widthCm;

    /**
     * 长度 (厘米)
     */
    private Integer lengthCm;

    /**
     * 限高 (厘米)
     */
    private Integer heightLimitCm;
    
    /**
     * 位置信息 (JSON)
     */
    private Map<String, Object> locationInfo;

    /**
     * 是否充电车位
     */
    private Boolean isCharging;
    
    /**
     * 充电设备 ID
     */
    private Long chargingDeviceId;

    /**
     * 占用车牌号
     */
    private String occupiedByPlate;

    /**
     * 占用开始时间
     */
    private LocalDateTime occupiedSince;

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
