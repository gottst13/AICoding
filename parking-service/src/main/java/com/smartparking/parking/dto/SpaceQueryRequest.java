package com.smartparking.parking.dto;

import lombok.Data;

/**
 * 车位分页查询请求
 */
@Data
public class SpaceQueryRequest {
    
    private Long zoneId;
    
    private Integer spaceType;
    
    private Integer status;
    
    private String spaceNo;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
