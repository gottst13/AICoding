package com.smartparking.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单分段 - 记录车辆在不同区域的停车时段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_segments")
public class OrderSegment {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;
    
    /**
     * 分段序号（从 1 开始）
     */
    @TableField("segment_no")
    private Integer segmentNo;
    
    /**
     * 区域 ID
     */
    @TableField("zone_id")
    private Long zoneId;
    
    /**
     * 区域名称
     */
    @TableField("zone_name")
    private String zoneName;
    
    /**
     * 进入区域时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /**
     * 离开区域时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;
    
    /**
     * 停车时长（分钟）
     */
    @TableField("duration_minutes")
    private Integer durationMinutes;
    
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
     * 计费规则快照（JSONB）
     */
    @TableField("fee_rule_snapshot")
    private Map<String, Object> feeRuleSnapshot;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
