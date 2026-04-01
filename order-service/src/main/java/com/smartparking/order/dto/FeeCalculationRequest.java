package com.smartparking.order.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 费用试算请求 DTO
 */
@Data
public class FeeCalculationRequest {
    
    @NotNull(message = "车场 ID 不能为空")
    private Long parkingLotId;
    
    @NotBlank(message = "车牌号不能为空")
    @Pattern(regexp = "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5}$", message = "车牌号格式不正确")
    private String plateNo;
    
    /**
     * 车辆类型：1-小型车 2-大型车
     */
    @NotNull(message = "车辆类型不能为空")
    @Min(value = 1, message = "车辆类型最小为 1")
    @Max(value = 2, message = "车辆类型最大为 2")
    private Integer vehicleType;
    
    /**
     * 入场时间
     */
    @NotNull(message = "入场时间不能为空")
    @Past(message = "入场时间必须是过去的时间")
    private LocalDateTime enterTime;
    
    /**
     * 出场时间（可选，不传则按当前时间计算）
     */
    @Future(message = "出场时间必须是未来的时间")
    private LocalDateTime exitTime;
    
    /**
     * 初始区域 ID（可选）
     */
    private Long initialZoneId;
    
    /**
     * 当前区域 ID（可选）
     */
    private Long currentZoneId;
}
