package com.smartparking.order.service.fee.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 计费结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeResult {
    
    /**
     * 总费用
     */
    private BigDecimal totalAmount;
    
    /**
     * 费用明细列表
     */
    private List<FeeDetail> feeDetails;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}
