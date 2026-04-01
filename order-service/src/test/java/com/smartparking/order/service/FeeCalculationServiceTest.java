package com.smartparking.order.service;

import com.smartparking.order.dto.FeeCalculationRequest;
import com.smartparking.order.dto.FeeCalculationVO;
import com.smartparking.order.service.fee.strategy.UnifiedFeeStrategy;
import com.smartparking.order.service.impl.FeeCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FeeCalculationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceTest {
    
    @Mock
    private FeeRuleService feeRuleService;
    
    @Mock
    private UnifiedFeeStrategy unifiedFeeStrategy;
    
    @InjectMocks
    private FeeCalculationServiceImpl feeCalculationService;
    
    private FeeCalculationRequest request;
    
    @BeforeEach
    void setUp() {
        request = new FeeCalculationRequest();
        request.setParkingLotId(1L);
        request.setPlateNo("京 A12345");
        request.setVehicleType(1);
        request.setEnterTime(LocalDateTime.now().minusHours(2));
    }
    
    @Test
    void testCalculate_Success() {
        // Arrange
        when(unifiedFeeStrategy.calculate(any())).thenReturn(
            com.smartparking.order.service.fee.strategy.FeeResult.builder()
                .totalAmount(new BigDecimal("20.00"))
                .feeDetails(java.util.Collections.emptyList())
                .build()
        );
        
        // Act
        FeeCalculationVO result = feeCalculationService.calculate(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());
        assertTrue(result.getDurationMinutes() > 0);
    }
    
    @Test
    void testCalculate_WithExitTime() {
        // Arrange
        request.setExitTime(LocalDateTime.now());
        
        when(unifiedFeeStrategy.calculate(any())).thenReturn(
            com.smartparking.order.service.fee.strategy.FeeResult.builder()
                .totalAmount(new BigDecimal("10.00"))
                .build()
        );
        
        // Act
        FeeCalculationVO result = feeCalculationService.calculate(request);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getDurationMinutes());
    }
}
