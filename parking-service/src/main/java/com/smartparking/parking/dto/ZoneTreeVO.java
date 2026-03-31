package com.smartparking.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 区域树形 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneTreeVO {
    
    private Long id;
    private String name;
    private String code;
    private Integer zoneType;
    private Integer floorLevel;
    private Integer availableSpaces;
    private Integer totalSpaces;
    
    /**
     * 子区域列表
     */
    private List<ZoneTreeVO> children;
}
