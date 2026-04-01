package com.smartparking.order.service;

import com.smartparking.order.dto.FeeCalculationRequest;
import com.smartparking.order.dto.FeeCalculationVO;
import com.smartparking.order.entity.ParkingOrder;

/**
 * 停车订单服务接口
 */
public interface ParkingOrderService {
    
    /**
     * 创建订单
     * @param plateNo 车牌号
     * @param parkingLotId 车场 ID
     * @param zoneId 初始区域 ID
     * @return 订单信息
     */
    ParkingOrder createOrder(String plateNo, Long parkingLotId, Long zoneId);
    
    /**
     * 更新订单计费模式
     * @param orderNo 订单号
     * @param feeMode 计费模式（1-统一计费，2-分区计费）
     */
    void updateFeeMode(String orderNo, Integer feeMode);
    
    /**
     * 计算订单费用
     * @param orderNo 订单号
     * @return 费用试算结果
     */
    FeeCalculationVO calculateOrderFee(String orderNo);
    
    /**
     * 完成订单
     * @param orderNo 订单号
     * @param totalAmount 总金额
     */
    void completeOrder(String orderNo, BigDecimal totalAmount);
}
