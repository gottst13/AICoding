package com.smartparking.parking.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.Map;

/**
 * 更新区域请求 DTO
 */
@Data
public class UpdateZoneRequest {
    
    @Size(max = 50, message = "区域名称长度不能超过 50 个字符")
    private String name;
    
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "区域编码只能包含大写字母、数字、下划线和短横线")
    @Size(max = 32, message = "区域编码长度不能超过 32 个字符")
    private String code;
    
    @Min(value = 1, message = "区域类型最小为 1")
    @Max(value = 2, message = "区域类型最大为 2")
    private Integer zoneType;
    
    private Integer zoneCategory;
    
    private Integer floorLevel;
    
    private Boolean hasIndependentExit;
    
    private Long[] exitLaneIds;
    
    private Map<String, Object> config;
    
    private Integer status;
}
