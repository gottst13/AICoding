package com.smartparking.order.service.impl;

import com.smartparking.common.exception.BusinessException;
import com.smartparking.order.dto.FeeCalculationRequest;
import com.smartparking.order.dto.FeeCalculationVO;
import com.smartparking.order.entity.OrderSegment;
import com.smartparking.order.entity.ParkingOrder;
import com.smartparking.order.mapper.ParkingOrderMapper;
import com.smartparking.order.repository.OrderSegmentRepository;
import com.smartparking.order.service.FeeCalculationService;
import com.smartparking.order.service.OrderSegmentService;
import com.smartparking.order.service.ParkingOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 停车订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingOrderServiceImpl implements ParkingOrderService {
    
    private final ParkingOrderMapper orderMapper;
    private final OrderSegmentRepository segmentRepository;
    private final OrderSegmentService segmentService;
    private final FeeCalculationService feeCalculationService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingOrder createOrder(String plateNo, Long parkingLotId, Long zoneId) {
        log.info("创建订单：plateNo={}, parkingLotId={}, zoneId={}", plateNo, parkingLotId, zoneId);
        
        // 1. 生成订单号
        String orderNo = generateOrderNo();
        
        // 2. 创建订单
        ParkingOrder order = ParkingOrder.builder()
            .orderNo(orderNo)
            .plateNo(plateNo)
            .parkingLotId(parkingLotId)
            .vehicleType(1) // TODO: 车型应该从车辆档案获取
            .enterTime(LocalDateTime.now())
            .initialZoneId(zoneId)
            .currentZoneId(zoneId)
            .feeMode(1) // 默认统一计费
            .status(0)  // 进行中
            .paymentStatus(0) // 未支付
            .build();
        
        int rows = orderMapper.insert(order);
        log.info("创建订单成功：orderNo={}, rows={}", orderNo, rows);
        
        // 3. 记录进入初始区域
        try {
            segmentService.enterZone(orderNo, zoneId, "入场区", LocalDateTime.now());
        } catch (Exception e) {
            log.error("记录进入区域失败：{}", e.getMessage());
        }
        
        return order;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFeeMode(String orderNo, Integer feeMode) {
        log.info("更新订单计费模式：orderNo={}, feeMode={}", orderNo, feeMode);
        
        ParkingOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("ORDER_001", "订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("ORDER_002", "订单已结束，无法修改计费模式");
        }
        
        // 检查是否已有分段记录
        List<OrderSegment> segments = segmentRepository.findByOrderNo(orderNo);
        if (!segments.isEmpty() && feeMode == 2) {
            // 如果已经有分段，可以切换到分区计费
            log.info("订单已有分段记录，支持分区计费");
        }
        
        order.setFeeMode(feeMode);
        orderMapper.updateById(order);
        log.info("更新计费模式成功：orderNo={}, feeMode={}", orderNo, feeMode);
    }
    
    @Override
    public FeeCalculationVO calculateOrderFee(String orderNo) {
        log.info("计算订单费用：orderNo={}", orderNo);
        
        ParkingOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("ORDER_001", "订单不存在");
        }
        
        // 构建试算请求
        FeeCalculationRequest request = new FeeCalculationRequest();
        request.setParkingLotId(order.getParkingLotId());
        request.setPlateNo(order.getPlateNo());
        request.setVehicleType(order.getVehicleType());
        request.setEnterTime(order.getEnterTime());
        
        // 根据计费模式选择计算方式
        if (order.getFeeMode() == 2) {
            // 分区计费：需要查询所有分段
            List<OrderSegment> segments = segmentRepository.findByOrderNo(orderNo);
            log.info("分区计费模式，共{}个分段", segments.size());
            // TODO: 使用分区计费策略计算
        } else {
            // 统一计费
            log.info("统一计费模式");
        }
        
        return feeCalculationService.calculate(request);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderNo, BigDecimal totalAmount) {
        log.info("完成订单：orderNo={}, totalAmount={}", orderNo, totalAmount);
        
        ParkingOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("ORDER_001", "订单不存在");
        }
        
        order.setStatus(1);  // 已完成
        order.setTotalAmount(totalAmount);
        order.setExitTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        log.info("订单完成：orderNo={}, amount={}", orderNo, totalAmount);
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
}
