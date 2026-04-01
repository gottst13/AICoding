package com.smartparking.order.service.fee.strategy;

import com.smartparking.common.exception.BusinessException;
import com.smartparking.order.context.ParkingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 统一计费策略 - 全场统一费率
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedFeeStrategy implements FeeStrategy {
    
    @Override
    public FeeResult calculate(ParkingContext context) {
        log.info("开始统一计费计算：orderNo={}, plateNo={}", 
            context.getOrderNo(), context.getPlateNo());
        
        // 1. 计算总时长
        LocalDateTime exitTime = context.getExitTime() != null ? 
            context.getExitTime() : LocalDateTime.now();
        Duration totalDuration = Duration.between(context.getEnterTime(), exitTime);
        long totalMinutes = totalDuration.toMinutes();
        
        log.info("停车时长：{} 分钟", totalMinutes);
        
        // 2. 获取车场统一计费规则（简化版本，实际应从数据库查询）
        // TODO: 从数据库获取计费规则
        BigDecimal hourlyRate = new BigDecimal("10.00"); // 每小时 10 元
        BigDecimal dailyMax = new BigDecimal("100.00");  // 每日封顶 100 元
        int freeMinutes = 30; // 免费时长 30 分钟
        
        List<FeeDetail> feeDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 3. 检查免费时长
        if (totalMinutes <= freeMinutes) {
            log.info("在免费时长内，免费放行");
            FeeDetail freeDetail = FeeDetail.builder()
                .itemName("免费停车")
                .durationMinutes((int) totalMinutes)
                .unitPrice(BigDecimal.ZERO)
                .quantity(1)
                .amount(BigDecimal.ZERO)
                .startTime(context.getEnterTime())
                .endTime(exitTime)
                .remark("免费时长内")
                .build();
            feeDetails.add(freeDetail);
            
            return FeeResult.builder()
                .totalAmount(BigDecimal.ZERO)
                .feeDetails(feeDetails)
                .build();
        }
        
        // 4. 计算收费时长（扣除免费时长）
        long chargeableMinutes = totalMinutes - freeMinutes;
        double hours = chargeableMinutes / 60.0;
        
        // 向上取整到小时
        int chargeableHours = (int) Math.ceil(hours);
        if (chargeableHours < 1) {
            chargeableHours = 1;
        }
        
        // 5. 计算费用
        BigDecimal calculatedAmount = hourlyRate.multiply(new BigDecimal(chargeableHours));
        
        // 6. 应用封顶价格
        if (calculatedAmount.compareTo(dailyMax) > 0) {
            totalAmount = dailyMax;
            log.info("触发封顶价格：{}", dailyMax);
        } else {
            totalAmount = calculatedAmount;
        }
        
        // 7. 生成费用明细
        FeeDetail detail = FeeDetail.builder()
            .itemName("停车费")
            .durationMinutes((int) chargeableMinutes)
            .unitPrice(hourlyRate)
            .quantity(chargeableHours)
            .amount(totalAmount)
            .startTime(context.getEnterTime())
            .endTime(exitTime)
            .remark(String.format("收费时长%d分钟，免费时长%d分钟", chargeableMinutes, freeMinutes))
            .build();
        feeDetails.add(detail);
        
        log.info("计费完成：总金额={}", totalAmount);
        
        return FeeResult.builder()
            .totalAmount(totalAmount)
            .feeDetails(feeDetails)
            .build();
    }
}
