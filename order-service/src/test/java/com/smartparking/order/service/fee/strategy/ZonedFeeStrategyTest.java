package com.smartparking.order.service.fee.strategy;

import com.smartparking.common.exception.BusinessException;
import com.smartparking.order.context.ParkingContext;
import com.smartparking.order.entity.OrderSegment;
import com.smartparking.order.repository.OrderSegmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 分区计费策略单元测试
 */
@ExtendWith(MockitoExtension.class)
class ZonedFeeStrategyTest {
    
    @Mock
    private OrderSegmentRepository segmentRepository;
    
    @InjectMocks
    private ZonedFeeStrategy zonedFeeStrategy;
    
    private ParkingContext context;
    private List<OrderSegment> segments;
    
    @BeforeEach
    void setUp() {
        context = new ParkingContext();
        context.setOrderNo("TEST_ORD_001");
        context.setPlateNo("京 A12345");
        context.setParkingLotId(1L);
        context.setVehicleType(1);
        
        // 创建两个分段
        segments = Arrays.asList(
            createSegment(1, 1L, "A 区", 
                LocalDateTime.of(2026, 4, 1, 10, 0),
                LocalDateTime.of(2026, 4, 1, 11, 0),
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                15
            ),
            createSegment(2, 2L, "B 区",
                LocalDateTime.of(2026, 4, 1, 11, 0),
                LocalDateTime.of(2026, 4, 1, 12, 0),
                new BigDecimal("8.00"),
                new BigDecimal("40.00"),
                15
            )
        );
    }
    
    @Test
    void testCalculate_Success() {
        // Arrange
        when(segmentRepository.findByOrderNo(anyString())).thenReturn(segments);
        
        // Act
        FeeResult result = zonedFeeStrategy.calculate(context);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isCrossZone());
        assertEquals(2, result.getFeeDetails().size());
        
        // A 区：1 小时，扣除 15 分钟免费，收费 45 分钟 ≈ 1 小时 = 10 元
        // B 区：1 小时，扣除 15 分钟免费，收费 45 分钟 ≈ 1 小时 = 8 元
        // 总计：18 元
        assertTrue(result.getTotalAmount().compareTo(new BigDecimal("18.00")) >= 0);
    }
    
    @Test
    void testCalculate_SingleZone() {
        // Arrange
        List<OrderSegment> singleSegment = Collections.singletonList(
            createSegment(1, 1L, "A 区",
                LocalDateTime.of(2026, 4, 1, 10, 0),
                LocalDateTime.of(2026, 4, 1, 11, 0),
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                15
            )
        );
        when(segmentRepository.findByOrderNo(anyString())).thenReturn(singleSegment);
        
        // Act
        FeeResult result = zonedFeeStrategy.calculate(context);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isCrossZone());
        assertEquals(1, result.getFeeDetails().size());
    }
    
    @Test
    void testCalculate_NoSegments() {
        // Arrange
        when(segmentRepository.findByOrderNo(anyString())).thenReturn(Collections.emptyList());
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            zonedFeeStrategy.calculate(context);
        });
    }
    
    @Test
    void testCalculate_WithinFreeDuration() {
        // Arrange
        List<OrderSegment> shortSegments = Collections.singletonList(
            createSegment(1, 1L, "A 区",
                LocalDateTime.of(2026, 4, 1, 10, 0),
                LocalDateTime.of(2026, 4, 1, 10, 10),  // 10 分钟 < 15 分钟免费
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                15
            )
        );
        when(segmentRepository.findByOrderNo(anyString())).thenReturn(shortSegments);
        
        // Act
        FeeResult result = zonedFeeStrategy.calculate(context);
        
        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
    }
    
    @Test
    void testCalculate_WithDailyMax() {
        // Arrange
        List<OrderSegment> longSegments = Collections.singletonList(
            createSegment(1, 1L, "A 区",
                LocalDateTime.of(2026, 4, 1, 8, 0),
                LocalDateTime.of(2026, 4, 1, 20, 0),  // 12 小时
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),  // 封顶 50 元
                15
            )
        );
        when(segmentRepository.findByOrderNo(anyString())).thenReturn(longSegments);
        
        // Act
        FeeResult result = zonedFeeStrategy.calculate(context);
        
        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getTotalAmount());
    }
    
    /**
     * 创建测试分段
     */
    private OrderSegment createSegment(int segmentNo, Long zoneId, String zoneName,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       BigDecimal hourlyRate, BigDecimal dailyMax,
                                       Integer freeMinutes) {
        OrderSegment segment = new OrderSegment();
        segment.setId((long) segmentNo);
        segment.setOrderNo(context.getOrderNo());
        segment.setSegmentNo(segmentNo);
        segment.setZoneId(zoneId);
        segment.setZoneName(zoneName);
        segment.setStartTime(startTime);
        segment.setEndTime(endTime);
        segment.setHourlyRate(hourlyRate);
        segment.setDailyMax(dailyMax);
        segment.setFreeMinutes(freeMinutes);
        
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("ruleName", "标准计费规则");
        segment.setFeeRuleSnapshot(snapshot);
        
        return segment;
    }
}
