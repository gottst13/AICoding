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
 * 停车场区域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "parking_zones", autoResultMap = true)
public class ParkingZone {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("parking_lot_id")
    private Long parkingLotId;
    
    @TableField("parent_zone_id")
    private Long parentZoneId;
    
    @TableField("name")
    private String name;
    
    @TableField("code")
    private String code;
    
    /**
     * 区域类型：1-主区域 2-子区域
     */
    @TableField("zone_type")
    private Integer zoneType;
    
    /**
     * 区域分类：1-商场区 2-办公区 3-酒店区 4-住宅区 5-充电区
     */
    @TableField("zone_category")
    private Integer zoneCategory;
    
    @TableField("floor_level")
    private Integer floorLevel;
    
    @TableField("total_spaces")
    private Integer totalSpaces;
    
    @TableField("available_spaces")
    private Integer availableSpaces;
    
    /**
     * 是否有独立出口
     */
    @TableField("has_independent_exit")
    private Boolean hasIndependentExit;
    
    /**
     * 关联的出口车道 ID 列表
     */
    @TableField(value = "exit_lane_ids", typeHandler = JacksonTypeHandler.class)
    private Long[] exitLaneIds;
    
    /**
     * 状态：0-停用 1-启用 2-已满
     */
    @TableField("status")
    private Integer status;
    
    @TableField(value = "config", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
