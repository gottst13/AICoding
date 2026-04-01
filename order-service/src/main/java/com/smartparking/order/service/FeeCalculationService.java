package com.smartparking.order.service;

import com.smartparking.order.dto.FeeCalculationRequest;
import com.smartparking.order.dto.FeeCalculationVO;

/**
 * 费用计算服务接口
 */
public interface FeeCalculationService {
    
    /**
     * 费用试算
     * @param request 试算请求
     * @return 试算结果
     */
    FeeCalculationVO calculate(FeeCalculationRequest request);
}
