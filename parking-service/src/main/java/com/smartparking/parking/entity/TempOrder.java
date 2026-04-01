package com.smartparking.parking.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临时订单实体类（用于计费计算）
 */
@Data
@Builder
public class TempOrder {

    /**
     * 停车场 ID
     */
    private Long parkingLotId;

    /**
     * 入场时间
     */
    private LocalDateTime enterTime;

    /**
     * 出场时间
     */
    private LocalDateTime exitTime;

    /**
     * 停车时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 车辆类型：1-小型车 2-大型车
     */
    private Integer vehicleType;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 车位 ID
     */
    private Long spaceId;

    /**
     * 区域 ID
     */
    private Long zoneId;
    
    /**
     * 订单号
     */
    private String orderNo;
}
