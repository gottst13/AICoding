package com.smartparking.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("payment_orders")
public class PaymentOrder {

    /**
     * 支付订单 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 停车订单号
     */
    private String orderNo;

    /**
     * 支付流水号 (第三方支付平台返回)
     */
    private String transactionId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付渠道：wechat/alipay/cash
     */
    private String channel;

    /**
     * 支付状态：0-待支付 1-支付成功 2-支付失败 3-已退款
     */
    private Integer status;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 用户 ID
     */
    private Long userId;

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
