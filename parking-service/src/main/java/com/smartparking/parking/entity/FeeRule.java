package com.smartparking.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 收费规则实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("fee_rules")
public class FeeRule {

    /**
     * 规则 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 停车场 ID
     */
    private Long parkingLotId;

    /**
     * 规则类型：1-按时长 2-按次 3-分时
     */
    private Integer ruleType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 车辆类型：1-小型车 2-大型车
     */
    private Integer vehicleType;

    /**
     * 每小时费率 (元)
     */
    private BigDecimal hourlyRate;

    /**
     * 每日封顶价格 (元)
     */
    private BigDecimal dailyMax;

    /**
     * 免费时长 (分钟)
     */
    private Integer freeMinutes;

    /**
     * 开始时间 (分时计费使用)
     */
    private LocalTime startTime;

    /**
     * 结束时间 (分时计费使用)
     */
    private LocalTime endTime;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 生效日期
     */
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    private LocalDate expiryDate;

    /**
     * 扩展配置 (JSON)
     */
    private String config;

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
