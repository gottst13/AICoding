package com.smartparking.parking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartparking.parking.entity.ParkingLot;
import com.smartparking.parking.service.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 停车场控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/parking-lots")
@Tag(name = "停车场管理接口")
public class ParkingLotController {

    @Autowired
    private ParkingLotService parkingLotService;

    /**
     * 分页查询停车场列表
     */
    @GetMapping
    @Operation(summary = "分页查询停车场")
    public Page<ParkingLot> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type
    ) {
        return parkingLotService.page(new Page<>(page, size));
    }

    /**
     * 根据 ID 查询停车场详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询停车场详情")
    public ParkingLot getById(@PathVariable Long id) {
        return parkingLotService.getById(id);
    }

    /**
     * 创建停车场
     */
    @PostMapping
    @Operation(summary = "创建停车场")
    public boolean save(@RequestBody ParkingLot parkingLot) {
        return parkingLotService.save(parkingLot);
    }

    /**
     * 更新停车场信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新停车场")
    public boolean update(@PathVariable Long id, @RequestBody ParkingLot parkingLot) {
        parkingLot.setId(id);
        return parkingLotService.updateById(parkingLot);
    }

    /**
     * 删除停车场
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除停车场")
    public boolean remove(@PathVariable Long id) {
        return parkingLotService.removeById(id);
    }
}
