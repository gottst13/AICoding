package com.smartparking.parking.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * 创建车位请求 DTO
 */
@Data
public class CreateSpaceRequest {
    
    @NotBlank(message = "车位编号不能为空")
    @Size(max = 20, message = "车位编号长度不能超过 20 个字符")
    private String spaceNo;
    
    @NotNull(message = "车位类型不能为空")
    @Min(value = 1, message = "车位类型最小为 1")
    @Max(value = 4, message = "车位类型最大为 4")
    private Integer spaceType;  // 1-小型车 2-大型车 3-无障碍 4-充电车位
    
    private Map<String, Object> locationInfo;
    
    private Integer widthCm;
    
    private Integer lengthCm;
    
    private Integer heightLimitCm;
}
