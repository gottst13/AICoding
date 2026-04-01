package com.smartparking.order.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 创建计费规则请求 DTO
 */
@Data
public class CreateFeeRuleRequest {
    
    @NotNull(message = "车场 ID 不能为空")
    private Long parkingLotId;
    
    @NotNull(message = "规则类型不能为空")
    @Min(value = 1, message = "规则类型最小为 1")
    @Max(value = 2, message = "规则类型最大为 2")
    private Integer ruleType;
    
    private Long zoneId;
    
    @NotNull(message = "车辆类型不能为空")
    @Min(value = 1, message = "车辆类型最小为 1")
    @Max(value = 2, message = "车辆类型最大为 2")
    private Integer vehicleType;
    
    @NotNull(message = "计费单价不能为空")
    @DecimalMin(value = "0.01", message = "计费单价必须大于 0")
    private BigDecimal hourlyRate;
    
    @DecimalMin(value = "0.01", message = "每日封顶价格必须大于 0")
    private BigDecimal dailyMax;
    
    @Min(value = 0, message = "免费时长最小为 0")
    private Integer freeMinutes = 30;
    
    private BigDecimal firstHourPrice;
    
    private Integer additionalUnitMinutes;
    
    private BigDecimal additionalUnitPrice;
}
