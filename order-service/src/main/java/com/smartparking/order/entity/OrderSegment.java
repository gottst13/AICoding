package com.smartparking.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单分段实体类 (记录车辆在不同区域的停车时段)
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("order_segments")
public class OrderSegment {

    /**
     * 分段 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 区域 ID
     */
    private Long zoneId;

    /**
     * 进入区域时间
     */
    private LocalDateTime enterTime;

    /**
     * 离开区域时间
     */
    private LocalDateTime exitTime;

    /**
     * 停车时长 (秒)
     */
    private Long durationSeconds;

    /**
     * 分段费用
     */
    private BigDecimal feeAmount;

    /**
     * 状态：0-进行中 1-已完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
