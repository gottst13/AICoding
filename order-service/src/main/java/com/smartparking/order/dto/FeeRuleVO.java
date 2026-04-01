package com.smartparking.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计费规则 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeRuleVO {
    
    private Long id;
    private Long parkingLotId;
    private Integer ruleType;
    private Long zoneId;
    private Integer vehicleType;
    private BigDecimal hourlyRate;
    private BigDecimal dailyMax;
    private Integer freeMinutes;
    private BigDecimal firstHourPrice;
    private Integer additionalUnitMinutes;
    private BigDecimal additionalUnitPrice;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
    private Integer status;
    private LocalDateTime createdAt;
}
