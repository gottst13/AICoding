package com.smartparking.order.service.impl;

import com.smartparking.order.context.ParkingContext;
import com.smartparking.order.dto.*;
import com.smartparking.order.entity.FeeRule;
import com.smartparking.order.service.FeeCalculationService;
import com.smartparking.order.service.FeeRuleService;
import com.smartparking.order.service.fee.strategy.FeeResult;
import com.smartparking.order.service.fee.strategy.FeeStrategy;
import com.smartparking.order.service.fee.strategy.UnifiedFeeStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 费用计算服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeCalculationServiceImpl implements FeeCalculationService {
    
    private final FeeRuleService feeRuleService;
    private final UnifiedFeeStrategy unifiedFeeStrategy;
    
    @Override
    public FeeCalculationVO calculate(FeeCalculationRequest request) {
        log.info("开始费用试算：plateNo={}, enterTime={}", 
            request.getPlateNo(), request.getEnterTime());
        
        // 1. 构建停车上下文
        ParkingContext context = buildParkingContext(request);
        
        // 2. 获取计费规则
        FeeRule rule = feeRuleService.getUnifiedRule(
            request.getParkingLotId(), 
            request.getVehicleType()
        );
        
        if (rule == null) {
            log.warn("未找到计费规则，使用默认规则：parkingLotId={}, vehicleType={}", 
                request.getParkingLotId(), request.getVehicleType());
            // TODO: 使用默认计费规则
        }
        
        // 3. 选择计费策略并计算
        FeeStrategy strategy = unifiedFeeStrategy; // 默认使用统一计费
        FeeResult result = strategy.calculate(context);
        
        // 4. 转换为 VO
        FeeCalculationVO response = convertToVO(result, context);
        
        log.info("费用试算完成：totalAmount={}, durationMinutes={}", 
            response.getTotalAmount(), response.getDurationMinutes());
        
        return response;
    }
    
    /**
     * 构建停车上下文
     */
    private ParkingContext buildParkingContext(FeeCalculationRequest request) {
        LocalDateTime exitTime = request.getExitTime();
        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }
        
        return ParkingContext.builder()
            .orderNo("TEMP_" + System.currentTimeMillis())
            .parkingLotId(request.getParkingLotId())
            .plateNo(request.getPlateNo())
            .vehicleType(request.getVehicleType())
            .enterTime(request.getEnterTime())
            .exitTime(exitTime)
            .initialZoneId(request.getInitialZoneId())
            .currentZoneId(request.getCurrentZoneId())
            .feeMode(1) // 默认统一计费
            .hasCrossZone(false)
            .crossZoneCount(0)
            .build();
    }
    
    /**
     * 转换为 VO
     */
    private FeeCalculationVO convertToVO(FeeResult result, ParkingContext context) {
        // 计算时长
        Duration duration = Duration.between(context.getEnterTime(), context.getExitTime());
        int durationMinutes = (int) duration.toMinutes();
        
        // 转换费用明细
        List<FeeDetailVO> detailVOs = new ArrayList<>();
        if (result.getFeeDetails() != null) {
            for (com.smartparking.order.service.fee.strategy.FeeDetail detail : result.getFeeDetails()) {
                FeeDetailVO detailVO = FeeDetailVO.builder()
                    .itemName(detail.getItemName())
                    .durationMinutes(detail.getDurationMinutes())
                    .unitPrice(detail.getUnitPrice())
                    .quantity(detail.getQuantity())
                    .amount(detail.getAmount())
                    .startTime(detail.getStartTime())
                    .endTime(detail.getEndTime())
                    .remark(detail.getRemark())
                    .build();
                detailVOs.add(detailVO);
            }
        }
        
        return FeeCalculationVO.builder()
            .totalAmount(result.getTotalAmount())
            .durationMinutes(durationMinutes)
            .feeMode(context.getFeeMode())
            .feeDetails(detailVOs)
            .remark(String.format("停车时长%d分钟", durationMinutes))
            .build();
    }
}
