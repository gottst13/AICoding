package com.smartparking.order.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 停车上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingContext {
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 车场 ID
     */
    private Long parkingLotId;
    
    /**
     * 车牌号
     */
    private String plateNo;
    
    /**
     * 车辆类型：1-小型车 2-大型车
     */
    private Integer vehicleType;
    
    /**
     * 入场时间
     */
    private LocalDateTime enterTime;
    
    /**
     * 出场时间
     */
    private LocalDateTime exitTime;
    
    /**
     * 初始区域 ID
     */
    private Long initialZoneId;
    
    /**
     * 当前区域 ID
     */
    private Long currentZoneId;
    
    /**
     * 计费模式：1-统一计费 2-分区计费
     */
    private Integer feeMode;
    
    /**
     * 是否有跨区移动
     */
    private Boolean hasCrossZone;
    
    /**
     * 跨区次数
     */
    private Integer crossZoneCount;
}
