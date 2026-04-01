package com.smartparking.order.service.impl;

import com.smartparking.common.exception.BusinessException;
import com.smartparking.order.entity.OrderSegment;
import com.smartparking.order.repository.OrderSegmentRepository;
import com.smartparking.order.service.FeeRuleService;
import com.smartparking.order.service.OrderSegmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单分段服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSegmentServiceImpl implements OrderSegmentService {
    
    private final OrderSegmentRepository segmentRepository;
    private final FeeRuleService feeRuleService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enterZone(String orderNo, Long zoneId, String zoneName, LocalDateTime enterTime) {
        log.info("车辆进入区域：orderNo={}, zoneId={}, zoneName={}", orderNo, zoneId, zoneName);
        
        // 1. 检查是否有未完成的上一个分段
        OrderSegment lastSegment = segmentRepository.findLastSegment(orderNo);
        
        if (lastSegment != null && lastSegment.getEndTime() == null) {
            // 如果上一个分段未结束，先结束它
            log.warn("上一个分段未结束，自动结束：segmentId={}", lastSegment.getId());
            exitZone(orderNo, lastSegment.getZoneId(), enterTime);
        }
        
        // 2. 获取该区域的计费规则
        var rule = feeRuleService.getUnifiedRule(zoneId, 1); // TODO: 车型应该从订单获取
        
        // 3. 创建新分段
        OrderSegment segment = OrderSegment.builder()
            .orderNo(orderNo)
            .zoneId(zoneId)
            .zoneName(zoneName)
            .startTime(enterTime)
            .hourlyRate(rule != null ? rule.getHourlyRate() : null)
            .dailyMax(rule != null ? rule.getDailyMax() : null)
            .freeMinutes(rule != null ? rule.getFreeMinutes() : null)
            .feeRuleSnapshot(rule != null ? convertRuleToMap(rule) : new HashMap<>())
            .build();
        
        int rows = segmentRepository.save(segment);
        log.info("创建分段成功：orderNo={}, zoneId={}, rows={}", orderNo, zoneId, rows);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitZone(String orderNo, Long zoneId, LocalDateTime exitTime) {
        log.info("车辆离开区域：orderNo={}, zoneId={}", orderNo, zoneId);
        
        // 1. 查询当前分段
        OrderSegment currentSegment = segmentRepository.findLastSegment(orderNo);
        
        if (currentSegment == null) {
            throw new BusinessException("SEGMENT_001", "没有找到当前分段");
        }
        
        if (!currentSegment.getZoneId().equals(zoneId)) {
            throw new BusinessException("SEGMENT_002", 
                String.format("区域不匹配，期望：%d, 实际：%d", currentSegment.getZoneId(), zoneId));
        }
        
        if (currentSegment.getEndTime() != null) {
            throw new BusinessException("SEGMENT_003", "分段已经结束");
        }
        
        // 2. 更新分段
        currentSegment.setEndTime(exitTime);
        long durationMinutes = Duration.between(
            currentSegment.getStartTime(), 
            exitTime
        ).toMinutes();
        currentSegment.setDurationMinutes((int) durationMinutes);
        
        int rows = segmentRepository.update(currentSegment);
        log.info("结束分段成功：orderNo={}, segmentId={}, duration={}min, rows={}", 
            orderNo, currentSegment.getId(), durationMinutes, rows);
    }
    
    @Override
    public List<OrderSegment> getSegmentsByOrderNo(String orderNo) {
        return segmentRepository.findByOrderNo(orderNo);
    }
    
    @Override
    public Long getCurrentZoneId(String orderNo) {
        OrderSegment lastSegment = segmentRepository.findLastSegment(orderNo);
        if (lastSegment != null && lastSegment.getEndTime() == null) {
            return lastSegment.getZoneId();
        }
        return null;
    }
    
    /**
     * 将计费规则转换为 Map
     */
    private Map<String, Object> convertRuleToMap(com.smartparking.order.entity.FeeRule rule) {
        Map<String, Object> map = new HashMap<>();
        map.put("ruleName", rule.getRuleName());
        map.put("ruleType", rule.getRuleType());
        map.put("hourlyRate", rule.getHourlyRate());
        map.put("dailyMax", rule.getDailyMax());
        map.put("freeMinutes", rule.getFreeMinutes());
        return map;
    }
}
