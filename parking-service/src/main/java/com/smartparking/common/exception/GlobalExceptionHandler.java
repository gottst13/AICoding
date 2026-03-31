package com.smartparking.common.exception;

import com.smartparking.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }
    
    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常：{}", e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }
    
    /**
     * 处理系统内部异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleInternalError(Exception e) {
        log.error("系统内部异常", e);
        return ApiResponse.error(500, "系统繁忙，请稍后再试");
    }
}
