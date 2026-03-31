package com.smartparking.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 车位实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "parking_spaces", autoResultMap = true)
public class ParkingSpace {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("zone_id")
    private Long zoneId;
    
    @TableField("space_no")
    private String spaceNo;
    
    /**
     * 车位类型：1-小型车 2-大型车 3-无障碍 4-充电车位
     */
    @TableField("space_type")
    private Integer spaceType;
    
    @TableField(value = "location_info", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> locationInfo;
    
    /**
     * 状态：0-占用 1-空闲 2-锁定 3-预约
     */
    @TableField("status")
    private Integer status;
    
    @TableField("occupied_by_plate")
    private String occupiedByPlate;
    
    @TableField("occupied_since")
    private LocalDateTime occupiedSince;
    
    @TableField("is_charging")
    private Boolean isCharging;
    
    @TableField("charging_device_id")
    private Long chargingDeviceId;
    
    @TableField("width_cm")
    private Integer widthCm;
    
    @TableField("length_cm")
    private Integer lengthCm;
    
    @TableField("height_limit_cm")
    private Integer heightLimitCm;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
