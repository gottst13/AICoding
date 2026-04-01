package com.smartparking.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 车位 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceVO {
    
    private Long id;
    private Long zoneId;
    private String spaceNo;
    private Integer spaceType;
    private Map<String, Object> locationInfo;
    private Integer status;
    private String occupiedByPlate;
    private LocalDateTime occupiedSince;
    private Boolean isCharging;
    private Long chargingDeviceId;
    private Integer widthCm;
    private Integer lengthCm;
    private Integer heightLimitCm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
