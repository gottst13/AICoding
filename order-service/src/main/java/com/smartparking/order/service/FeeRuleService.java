package com.smartparking.order.service;

import com.smartparking.order.dto.CreateFeeRuleRequest;
import com.smartparking.order.dto.FeeRuleVO;
import com.smartparking.order.entity.FeeRule;

import java.util.List;

/**
 * 计费规则服务接口
 */
public interface FeeRuleService {
    
    /**
     * 创建计费规则
     */
    FeeRuleVO createFeeRule(CreateFeeRuleRequest request);
    
    /**
     * 更新计费规则
     */
    FeeRuleVO updateFeeRule(Long ruleId, CreateFeeRuleRequest request);
    
    /**
     * 删除计费规则
     */
    void deleteFeeRule(Long ruleId);
    
    /**
     * 获取计费规则详情
     */
    FeeRuleVO getFeeRule(Long ruleId);
    
    /**
     * 查询车场的计费规则列表
     */
    List<FeeRuleVO> queryFeeRules(Long parkingLotId);
    
    /**
     * 获取统一计费规则
     */
    FeeRule getUnifiedRule(Long parkingLotId, Integer vehicleType);
}
