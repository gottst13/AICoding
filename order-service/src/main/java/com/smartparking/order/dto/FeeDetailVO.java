package com.smartparking.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用明细 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeDetailVO {
    
    /**
     * 计费项名称
     */
    private String itemName;
    
    /**
     * 计费时长（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 小计金额
     */
    private BigDecimal amount;
    
    /**
     * 计费开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 计费结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 备注说明
     */
    private String remark;
}
