package com.smartparking.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartparking.common.exception.BusinessException;
import com.smartparking.order.dto.CreateFeeRuleRequest;
import com.smartparking.order.dto.FeeRuleVO;
import com.smartparking.order.entity.FeeRule;
import com.smartparking.order.mapper.FeeRuleMapper;
import com.smartparking.order.service.FeeRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 计费规则服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeRuleServiceImpl implements FeeRuleService {
    
    private final FeeRuleMapper feeRuleMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeeRuleVO createFeeRule(CreateFeeRuleRequest request) {
        log.info("开始创建计费规则：parkingLotId={}, ruleType={}", 
            request.getParkingLotId(), request.getRuleType());
        
        // 1. 检查是否已存在相同规则的计费规则
        LambdaQueryWrapper<FeeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeRule::getParkingLotId, request.getParkingLotId())
               .eq(FeeRule::getRuleType, request.getRuleType())
               .eq(FeeRule::getVehicleType, request.getVehicleType());
        
        if (request.getZoneId() != null) {
            wrapper.eq(FeeRule::getZoneId, request.getZoneId());
        } else {
            wrapper.isNull(FeeRule::getZoneId);
        }
        
        Long count = feeRuleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("FEE_RULE_001", "已存在相同的计费规则");
        }
        
        // 2. 创建计费规则
        FeeRule rule = FeeRule.builder()
            .parkingLotId(request.getParkingLotId())
            .ruleType(request.getRuleType())
            .zoneId(request.getZoneId())
            .vehicleType(request.getVehicleType())
            .hourlyRate(request.getHourlyRate())
            .dailyMax(request.getDailyMax())
            .freeMinutes(request.getFreeMinutes())
            .firstHourPrice(request.getFirstHourPrice())
            .additionalUnitMinutes(request.getAdditionalUnitMinutes())
            .additionalUnitPrice(request.getAdditionalUnitPrice())
            .status(1)
            .build();
        
        int rows = feeRuleMapper.insert(rule);
        if (rows != 1) {
            throw new BusinessException("FEE_RULE_002", "创建计费规则失败");
        }
        
        log.info("计费规则创建成功：id={}", rule.getId());
        return convertToVO(rule);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeeRuleVO updateFeeRule(Long ruleId, CreateFeeRuleRequest request) {
        log.info("更新计费规则：ruleId={}", ruleId);
        
        FeeRule existingRule = feeRuleMapper.selectById(ruleId);
        if (existingRule == null) {
            throw new BusinessException("FEE_RULE_NOT_FOUND", "计费规则不存在");
        }
        
        BeanUtils.copyProperties(request, existingRule);
        existingRule.setUpdatedAt(LocalDateTime.now());
        
        int rows = feeRuleMapper.updateById(existingRule);
        if (rows != 1) {
            throw new BusinessException("FEE_RULE_003", "更新计费规则失败");
        }
        
        log.info("计费规则更新成功：ruleId={}", ruleId);
        return convertToVO(existingRule);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeeRule(Long ruleId) {
        log.info("删除计费规则：ruleId={}", ruleId);
        
        FeeRule existingRule = feeRuleMapper.selectById(ruleId);
        if (existingRule == null) {
            throw new BusinessException("FEE_RULE_NOT_FOUND", "计费规则不存在");
        }
        
        int rows = feeRuleMapper.deleteById(ruleId);
        if (rows != 1) {
            throw new BusinessException("FEE_RULE_004", "删除计费规则失败");
        }
        
        log.info("计费规则删除成功：ruleId={}", ruleId);
    }
    
    @Override
    public FeeRuleVO getFeeRule(Long ruleId) {
        FeeRule rule = feeRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException("FEE_RULE_NOT_FOUND", "计费规则不存在");
        }
        return convertToVO(rule);
    }
    
    @Override
    public List<FeeRuleVO> queryFeeRules(Long parkingLotId) {
        LambdaQueryWrapper<FeeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeRule::getParkingLotId, parkingLotId)
               .eq(FeeRule::getStatus, 1)
               .orderByAsc(FeeRule::getRuleType, FeeRule::getZoneId);
        
        List<FeeRule> rules = feeRuleMapper.selectList(wrapper);
        return rules.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    @Override
    public FeeRule getUnifiedRule(Long parkingLotId, Integer vehicleType) {
        return feeRuleMapper.selectUnifiedRule(parkingLotId, vehicleType, LocalDateTime.now());
    }
    
    /**
     * 转换为 VO
     */
    private FeeRuleVO convertToVO(FeeRule rule) {
        return FeeRuleVO.builder()
            .id(rule.getId())
            .parkingLotId(rule.getParkingLotId())
            .ruleType(rule.getRuleType())
            .zoneId(rule.getZoneId())
            .vehicleType(rule.getVehicleType())
            .hourlyRate(rule.getHourlyRate())
            .dailyMax(rule.getDailyMax())
            .freeMinutes(rule.getFreeMinutes())
            .firstHourPrice(rule.getFirstHourPrice())
            .additionalUnitMinutes(rule.getAdditionalUnitMinutes())
            .additionalUnitPrice(rule.getAdditionalUnitPrice())
            .effectiveTime(rule.getEffectiveTime())
            .expireTime(rule.getExpireTime())
            .status(rule.getStatus())
            .createdAt(rule.getCreatedAt())
            .build();
    }
}
