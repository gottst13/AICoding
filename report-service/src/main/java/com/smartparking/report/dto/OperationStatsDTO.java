package com.smartparking.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 运营统计 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationStatsDTO {

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 今日订单数
     */
    private Integer todayOrders;

    /**
     * 总收入
     */
    private BigDecimal totalRevenue;

    /**
     * 今日收入
     */
    private BigDecimal todayRevenue;

    /**
     * 平均客单价
     */
    private BigDecimal avgOrderValue;

    /**
     * 车场总数
     */
    private Integer parkingLotsCount;

    /**
     * 车道总数
     */
    private Integer lanesCount;

    /**
     * 设备在线数
     */
    private Integer devicesOnline;
}
