package com.smartparking.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 临时停车订单实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("temp_orders")
public class TempOrder {

    /**
     * 订单 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号 (格式：TP+ 时间戳 + 随机数)
     */
    private String orderNo;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 停车场 ID
     */
    private Long parkingLotId;

    /**
     * 入口车道 ID
     */
    private Long laneId;

    /**
     * 入场时间
     */
    private LocalDateTime enterTime;

    /**
     * 出场时间
     */
    private LocalDateTime exitTime;

    /**
     * 停车时长 (秒)
     */
    private Long durationSeconds;

    /**
     * 原始金额
     */
    private BigDecimal originalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 应付金额
     */
    private BigDecimal payableAmount;

    /**
     * 实付金额
     */
    private BigDecimal paidAmount;

    /**
     * 订单状态：0-已入场 1-待支付 2-已支付 3-已出场 4-欠费
     */
    private Integer status;

    /**
     * 支付渠道：wechat/alipay
     */
    private String paymentChannel;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 扩展信息 (JSON)
     */
    private String metadata;

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
