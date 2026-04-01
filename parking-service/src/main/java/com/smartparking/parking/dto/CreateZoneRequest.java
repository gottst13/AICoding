package com.smartparking.parking.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * 创建区域请求 DTO
 */
@Data
public class CreateZoneRequest {
    
    @NotBlank(message = "区域名称不能为空")
    @Size(max = 50, message = "区域名称长度不能超过 50 个字符")
    private String name;
    
    @NotBlank(message = "区域编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "区域编码只能包含大写字母、数字、下划线和短横线")
    @Size(max = 32, message = "区域编码长度不能超过 32 个字符")
    private String code;
    
    @NotNull(message = "区域类型不能为空")
    @Min(value = 1, message = "区域类型最小为 1")
    @Max(value = 2, message = "区域类型最大为 2")
    private Integer zoneType;  // 1-主区域 2-子区域
    
    private Integer zoneCategory;  // 1-商场区 2-办公区...
    
    private Integer floorLevel;
    
    private Boolean hasIndependentExit;
    
    private Long[] exitLaneIds;
    
    private Map<String, Object> config;
    
    /**
     * 父区域 ID（仅子区域需要）
     */
    private Long parentZoneId;
}
