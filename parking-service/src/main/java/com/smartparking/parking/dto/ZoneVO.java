package com.smartparking.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 区域 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneVO {
    
    private Long id;
    private Long parkingLotId;
    private Long parentZoneId;
    private String name;
    private String code;
    private Integer zoneType;
    private Integer zoneCategory;
    private Integer floorLevel;
    private Integer totalSpaces;
    private Integer availableSpaces;
    private Boolean hasIndependentExit;
    private Long[] exitLaneIds;
    private Integer status;
    private Map<String, Object> config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
