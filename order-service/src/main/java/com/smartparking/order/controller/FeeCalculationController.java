package com.smartparking.order.controller;

import com.smartparking.common.response.ApiResponse;
import com.smartparking.order.dto.FeeCalculationRequest;
import com.smartparking.order.dto.FeeCalculationVO;
import com.smartparking.order.service.FeeCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 费用试算 Controller
 */
@RestController
@RequestMapping("/api/v1/fee/calculate")
@RequiredArgsConstructor
public class FeeCalculationController {
    
    private final FeeCalculationService feeCalculationService;
    
    /**
     * 费用试算
     */
    @PostMapping
    public ApiResponse<FeeCalculationVO> calculate(@Validated @RequestBody FeeCalculationRequest request) {
        FeeCalculationVO result = feeCalculationService.calculate(request);
        return ApiResponse.success(result);
    }
}
