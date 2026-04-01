package com.smartparking.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartparking.order.entity.TempOrder;
import com.smartparking.order.service.TempOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@Api(tags = "订单管理接口")
public class OrderController {

    @Autowired
    private TempOrderService tempOrderService;

    /**
     * 分页查询订单列表
     */
    @GetMapping
    @ApiOperation("分页查询订单")
    public Page<TempOrder> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String plateNo,
            @RequestParam(required = false) Integer status
    ) {
        return tempOrderService.page(new Page<>(page, size));
    }

    /**
     * 根据订单号查询订单详情
     */
    @GetMapping("/{orderNo}")
    @ApiOperation("查询订单详情")
    public TempOrder getByOrderNo(@PathVariable String orderNo) {
        return tempOrderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TempOrder>()
                .eq(TempOrder::getOrderNo, orderNo));
    }

    /**
     * 根据车牌号查询当前在场订单
     */
    @GetMapping("/plate/{plateNo}/current")
    @ApiOperation("查询当前在场订单")
    public TempOrder getCurrentOrderByPlate(@PathVariable String plateNo) {
        return tempOrderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TempOrder>()
                .eq(TempOrder::getPlateNo, plateNo)
                .in(TempOrder::getStatus, 0, 1)); // 已入场或待支付状态
    }

    /**
     * 创建订单 (入场)
     */
    @PostMapping("/entry")
    @ApiOperation("车辆入场创建订单")
    public boolean entry(@RequestBody TempOrder order) {
        return tempOrderService.save(order);
    }

    /**
     * 更新订单 (出场)
     */
    @PutMapping("/{orderNo}/exit")
    @ApiOperation("车辆出场更新订单")
    public boolean exit(
            @PathVariable String orderNo,
            @RequestParam LocalDateTime exitTime
    ) {
        TempOrder order = tempOrderService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TempOrder>()
                .eq(TempOrder::getOrderNo, orderNo));
        if (order != null) {
            order.setExitTime(exitTime);
            order.setStatus(3); // 已出场
            return tempOrderService.updateById(order);
        }
        return false;
    }
}
