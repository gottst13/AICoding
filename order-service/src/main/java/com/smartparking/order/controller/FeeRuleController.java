package com.smartparking.order.controller;

import com.smartparking.common.response.ApiResponse;
import com.smartparking.order.dto.CreateFeeRuleRequest;
import com.smartparking.order.dto.FeeRuleVO;
import com.smartparking.order.service.FeeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计费规则管理 Controller
 */
@RestController
@RequestMapping("/api/v1/fee-rules")
@RequiredArgsConstructor
public class FeeRuleController {
    
    private final FeeRuleService feeRuleService;
    
    /**
     * 创建计费规则
     */
    @PostMapping
    public ApiResponse<FeeRuleVO> createFeeRule(@Validated @RequestBody CreateFeeRuleRequest request) {
        FeeRuleVO rule = feeRuleService.createFeeRule(request);
        return ApiResponse.success(rule);
    }
    
    /**
     * 更新计费规则
     */
    @PutMapping("/{id}")
    public ApiResponse<FeeRuleVO> updateFeeRule(
        @PathVariable Long id,
        @Validated @RequestBody CreateFeeRuleRequest request
    ) {
        FeeRuleVO rule = feeRuleService.updateFeeRule(id, request);
        return ApiResponse.success(rule);
    }
    
    /**
     * 删除计费规则
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFeeRule(@PathVariable Long id) {
        feeRuleService.deleteFeeRule(id);
        return ApiResponse.success();
    }
    
    /**
     * 获取计费规则详情
     */
    @GetMapping("/{id}")
    public ApiResponse<FeeRuleVO> getFeeRule(@PathVariable Long id) {
        FeeRuleVO rule = feeRuleService.getFeeRule(id);
        return ApiResponse.success(rule);
    }
    
    /**
     * 查询车场的计费规则列表
     */
    @GetMapping("/parking-lot/{parkingLotId}")
    public ApiResponse<List<FeeRuleVO>> queryFeeRules(@PathVariable Long parkingLotId) {
        List<FeeRuleVO> rules = feeRuleService.queryFeeRules(parkingLotId);
        return ApiResponse.success(rules);
    }
}
