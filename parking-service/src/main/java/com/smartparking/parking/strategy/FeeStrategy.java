package com.smartparking.parking.strategy;

import com.smartparking.parking.entity.FeeRule;
import com.smartparking.parking.entity.TempOrder;

import java.math.BigDecimal;

/**
 * 计费策略接口
 */
public interface FeeStrategy {

    /**
     * 计算停车费
     * 
     * @param order 订单信息
     * @param feeRule 收费规则
     * @return 应付金额
     */
    BigDecimal calculate(TempOrder order, FeeRule feeRule);

    /**
     * 策略名称
     */
    String getName();
}
