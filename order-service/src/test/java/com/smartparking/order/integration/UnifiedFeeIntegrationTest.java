package com.smartparking.order.integration;

import com.smartparking.order.entity.ParkingOrder;
import com.smartparking.order.entity.OrderSegment;
import com.smartparking.order.service.ParkingOrderService;
import com.smartparking.order.service.OrderSegmentService;
import com.smartparking.order.service.FeeCalculationService;
import com.smartparking.order.dto.FeeCalculationRequest;
import com.smartparking.order.dto.FeeCalculationVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全链路集成测试 - 统一计费场景
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class UnifiedFeeIntegrationTest {
    
    @Autowired
    private ParkingOrderService orderService;
    
    @Autowired
    private OrderSegmentService segmentService;
    
    @Autowired
    private FeeCalculationService feeCalculationService;
    
    @Test
    void testUnifiedFee_FullProcess() {
        log.info("========== 开始统一计费全链路测试 ==========");
        
        // Step 1: 创建订单（车辆入场）
        String plateNo = "京 A88888";
        Long parkingLotId = 1L;
        Long zoneId = 1L;
        
        ParkingOrder order = orderService.createOrder(plateNo, parkingLotId, zoneId);
        assertNotNull(order);
        assertNotNull(order.getOrderNo());
        assertEquals(0, order.getStatus()); // 进行中
        log.info("✓ 订单创建成功：{}", order.getOrderNo());
        
        // Step 2: 模拟车辆在区域内停车（2 小时）
        LocalDateTime exitTime = order.getEnterTime().plusHours(2);
        
        // Step 3: 计算费用
        FeeCalculationVO feeResult = orderService.calculateOrderFee(order.getOrderNo());
        assertNotNull(feeResult);
        assertTrue(feeResult.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) > 0);
        log.info("✓ 费用计算成功：{}元，时长{}分钟", 
            feeResult.getTotalAmount(), feeResult.getDurationMinutes());
        
        // Step 4: 完成订单
        orderService.completeOrder(order.getOrderNo(), feeResult.getTotalAmount());
        
        // 验证订单状态
        // TODO: 需要查询订单验证状态
        
        log.info("========== 统一计费全链路测试完成 ==========");
    }
}
