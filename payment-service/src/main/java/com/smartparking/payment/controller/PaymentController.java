package com.smartparking.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartparking.payment.entity.PaymentOrder;
import com.smartparking.payment.service.PaymentOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@Api(tags = "支付管理接口")
public class PaymentController {

    @Autowired
    private PaymentOrderService paymentOrderService;

    /**
     * 分页查询支付订单列表
     */
    @GetMapping
    @ApiOperation("分页查询支付订单")
    public Page<PaymentOrder> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status
    ) {
        return paymentOrderService.page(new Page<>(page, size));
    }

    /**
     * 根据停车订单号查询支付信息
     */
    @GetMapping("/{orderNo}")
    @ApiOperation("查询支付信息")
    public PaymentOrder getByOrderNo(@PathVariable String orderNo) {
        return paymentOrderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOrderNo, orderNo));
    }

    /**
     * 创建支付订单
     */
    @PostMapping
    @ApiOperation("创建支付订单")
    public boolean create(@RequestBody PaymentOrder paymentOrder) {
        return paymentOrderService.save(paymentOrder);
    }

    /**
     * 更新支付状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新支付状态")
    public boolean updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String transactionId
    ) {
        PaymentOrder paymentOrder = paymentOrderService.getById(id);
        if (paymentOrder != null) {
            paymentOrder.setStatus(status);
            if (transactionId != null) {
                paymentOrder.setTransactionId(transactionId);
            }
            if (status == 1) { // 支付成功
                paymentOrder.setPaymentTime(java.time.LocalDateTime.now());
            }
            return paymentOrderService.updateById(paymentOrder);
        }
        return false;
    }
}
