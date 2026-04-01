package com.smartparking.parking.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 费用计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeResult {

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
     * 计费规则说明
     */
    private String ruleDescription;

    /**
     * 停车时长 (秒)
     */
    private Long durationSeconds;
}
