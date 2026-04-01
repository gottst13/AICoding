package com.smartparking.order.service.fee.strategy;

import com.smartparking.common.exception.BusinessException;
import com.smartparking.order.context.ParkingContext;
import com.smartparking.order.entity.OrderSegment;
import com.smartparking.order.repository.OrderSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 分区计费策略 - 各区域独立计费
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZonedFeeStrategy implements FeeStrategy {
    
    private final OrderSegmentRepository segmentRepository;
    
    @Override
    public FeeResult calculate(ParkingContext context) {
        log.info("开始分区计费计算：orderNo={}, plateNo={}", 
            context.getOrderNo(), context.getPlateNo());
        
        // 1. 获取所有分段
        List<OrderSegment> segments = segmentRepository.findByOrderNo(
            context.getOrderNo()
        );
        
        if (segments.isEmpty()) {
            throw new BusinessException("FEE_003", "订单没有分段信息");
        }
        
        log.info("找到{}个分段", segments.size());
        
        List<FeeDetail> feeDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 2. 逐段计算费用
        for (OrderSegment segment : segments) {
            BigDecimal segmentFee = calculateSegmentFee(segment);
            totalAmount = totalAmount.add(segmentFee);
            
            FeeDetail detail = FeeDetail.builder()
                .zoneId(segment.getZoneId())
                .zoneName(segment.getZoneName())
                .durationMinutes((int) Duration.between(
                    segment.getStartTime(), segment.getEndTime()
                ).toMinutes())
                .unitPrice(segment.getHourlyRate())
                .amount(segmentFee)
                .remark(String.format("分段 %d: %s", segment.getSegmentNo(), segment.getZoneName()))
                .build();
            
            feeDetails.add(detail);
            log.debug("分段{}费用：zoneId={}, duration={}min, amount={}", 
                segment.getSegmentNo(), segment.getZoneId(), 
                detail.getDurationMinutes(), segmentFee);
        }
        
        boolean hasCrossZone = segments.size() > 1;
        log.info("分区计费完成：totalAmount={}, segments={}, hasCrossZone={}", 
            totalAmount, segments.size(), hasCrossZone);
        
        return FeeResult.builder()
            .totalAmount(totalAmount)
            .feeDetails(feeDetails)
            .crossZone(hasCrossZone)
            .build();
    }
    
    /**
     * 计算单个分段的费用
     */
    private BigDecimal calculateSegmentFee(OrderSegment segment) {
        // 1. 获取分段计费参数
        BigDecimal hourlyRate = segment.getHourlyRate();
        BigDecimal dailyMax = segment.getDailyMax();
        Integer freeMinutes = segment.getFreeMinutes();
        
        // 2. 计算时长（分钟）
        long durationMinutes = Duration.between(
            segment.getStartTime(), 
            segment.getEndTime()
        ).toMinutes();
        
        // 3. 检查免费时长
        if (freeMinutes != null && durationMinutes <= freeMinutes) {
            return BigDecimal.ZERO;
        }
        
        // 4. 计算收费时长
        long chargeableMinutes = freeMinutes != null ? 
            durationMinutes - freeMinutes : durationMinutes;
        
        // 5. 按小时计费（向上取整）
        int chargeableHours = (int) Math.ceil(chargeableMinutes / 60.0);
        BigDecimal calculatedFee = hourlyRate.multiply(new BigDecimal(chargeableHours));
        
        // 6. 应用封顶价格
        if (dailyMax != null && calculatedFee.compareTo(dailyMax) > 0) {
            return dailyMax;
        }
        
        return calculatedFee;
    }
}
