package com.smartparking.parking.service;

import com.smartparking.parking.entity.FeeRule;
import com.smartparking.parking.entity.TempOrder;

import java.math.BigDecimal;

/**
 * 计费服务接口
 */
public interface FeeCalculationService {

    /**
     * 计算停车费
     * 
     * @param order 订单信息
     * @return 费用结果
     */
    FeeResult calculate(TempOrder order);

    /**
     * 费用试算 (不保存订单)
     */
    FeeResult tryCalculate(TempOrder order, Long parkingLotId);
}
