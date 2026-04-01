package com.smartparking.order.integration;

import com.smartparking.order.entity.ParkingOrder;
import com.smartparking.order.service.ParkingOrderService;
import com.smartparking.order.service.OrderSegmentService;
import com.smartparking.order.dto.FeeCalculationVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全链路集成测试 - 分区计费场景（跨区停车）
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ZonedFeeIntegrationTest {
    
    @Autowired
    private ParkingOrderService orderService;
    
    @Autowired
    private OrderSegmentService segmentService;
    
    @Test
    void testZonedFee_CrossZoneParking() {
        log.info("========== 开始分区计费全链路测试（跨区停车） ==========");
        
        // Step 1: 创建订单（车辆进入 A 区）
        String plateNo = "京 B99999";
        Long parkingLotId = 1L;
        Long zoneA = 1L;
        Long zoneB = 2L;
        
        ParkingOrder order = orderService.createOrder(plateNo, parkingLotId, zoneA);
        assertNotNull(order);
        assertEquals(zoneA, order.getInitialZoneId());
        log.info("✓ 订单创建成功，进入 A 区：{}", order.getOrderNo());
        
        // Step 2: 更新计费模式为分区计费
        orderService.updateFeeMode(order.getOrderNo(), 2);
        log.info("✓ 计费模式已切换为分区计费");
        
        // Step 3: 车辆从 A 区移动到 B 区（1 小时后）
        LocalDateTime moveToZoneB = order.getEnterTime().plusHours(1);
        segmentService.exitZone(order.getOrderNo(), zoneA, moveToZoneB);
        segmentService.enterZone(order.getOrderNo(), zoneB, "B 区", moveToZoneB);
        log.info("✓ 车辆从 A 区移动到 B 区");
        
        // Step 4: 继续在 B 区停车 1 小时
        LocalDateTime exitTime = moveToZoneB.plusHours(1);
        
        // Step 5: 计算费用
        FeeCalculationVO feeResult = orderService.calculateOrderFee(order.getOrderNo());
        assertNotNull(feeResult);
        assertTrue(feeResult.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) > 0);
        log.info("✓ 费用计算成功：{}元，总时长{}分钟", 
            feeResult.getTotalAmount(), feeResult.getDurationMinutes());
        
        // Step 6: 完成订单
        orderService.completeOrder(order.getOrderNo(), feeResult.getTotalAmount());
        log.info("✓ 订单已完成");
        
        log.info("========== 分区计费全链路测试完成 ==========");
    }
    
    @Test
    void testZonedFee_SingleZone() {
        log.info("========== 开始分区计费测试（单区域） ==========");
        
        String plateNo = "京 C66666";
        Long parkingLotId = 1L;
        Long zoneId = 1L;
        
        ParkingOrder order = orderService.createOrder(plateNo, parkingLotId, zoneId);
        orderService.updateFeeMode(order.getOrderNo(), 2);
        
        log.info("✓ 订单创建并设置为分区计费模式");
        
        // 一直在同一区域停车
        FeeCalculationVO feeResult = orderService.calculateOrderFee(order.getOrderNo());
        assertNotNull(feeResult);
        log.info("✓ 单区域费用计算成功：{}元", feeResult.getTotalAmount());
        
        log.info("========== 单区域测试完成 ==========");
    }
}
