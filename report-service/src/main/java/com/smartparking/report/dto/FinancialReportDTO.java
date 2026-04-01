package com.smartparking.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 财务报表 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportDTO {

    /**
     * 报表日期
     */
    private LocalDate reportDate;

    /**
     * 总收入
     */
    private BigDecimal totalIncome;

    /**
     * 现金支付
     */
    private BigDecimal cashPayment;

    /**
     * 微信支付
     */
    private BigDecimal wechatPayment;

    /**
     * 支付宝支付
     */
    private BigDecimal alipayPayment;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 订单总数
     */
    private Integer orderCount;

    /**
     * 平均客单价
     */
    private BigDecimal avgOrderValue;
}
