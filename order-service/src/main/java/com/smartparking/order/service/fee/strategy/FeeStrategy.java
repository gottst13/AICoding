package com.smartparking.order.service.fee.strategy;

import com.smartparking.order.context.ParkingContext;

/**
 * 计费策略接口
 */
public interface FeeStrategy {
    
    /**
     * 计算停车费用
     * @param context 停车上下文
     * @return 应收费用
     */
    FeeResult calculate(ParkingContext context);
}
