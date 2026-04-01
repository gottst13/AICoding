package com.smartparking.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计费规则实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fee_rules")
public class FeeRule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("parking_lot_id")
    private Long parkingLotId;
    
    /**
     * 规则类型：1-统一计费 2-分区计费
     */
    @TableField("rule_type")
    private Integer ruleType;
    
    @TableField("zone_id")
    private Long zoneId;
    
    /**
     * 车辆类型：1-小型车 2-大型车
     */
    @TableField("vehicle_type")
    private Integer vehicleType;
    
    /**
     * 计费单价（元/小时）
     */
    @TableField("hourly_rate")
    private BigDecimal hourlyRate;
    
    /**
     * 每日封顶价格
     */
    @TableField("daily_max")
    private BigDecimal dailyMax;
    
    /**
     * 免费时长（分钟）
     */
    @TableField("free_minutes")
    private Integer freeMinutes;
    
    /**
     * 首小时价格
     */
    @TableField("first_hour_price")
    private BigDecimal firstHourPrice;
    
    /**
     * 续时单位（分钟）
     */
    @TableField("additional_unit_minutes")
    private Integer additionalUnitMinutes;
    
    /**
     * 续时单位价格
     */
    @TableField("additional_unit_price")
    private BigDecimal additionalUnitPrice;
    
    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;
    
    /**
     * 失效时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    
    /**
     * 状态：0-停用 1-启用
     */
    @TableField("status")
    private Integer status;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
