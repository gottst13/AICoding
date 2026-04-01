package com.smartparking.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用试算结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeCalculationVO {
    
    /**
     * 总费用
     */
    private BigDecimal totalAmount;
    
    /**
     * 停车时长（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 计费模式：1-统一计费 2-分区计费
     */
    private Integer feeMode;
    
    /**
     * 费用明细列表
     */
    private List<FeeDetailVO> feeDetails;
    
    /**
     * 备注说明
     */
    private String remark;
}
