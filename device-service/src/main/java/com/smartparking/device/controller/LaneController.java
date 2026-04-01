package com.smartparking.device.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartparking.device.entity.Lane;
import com.smartparking.device.service.LaneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 车道控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/lanes")
@Api(tags = "车道管理接口")
public class LaneController {

    @Autowired
    private LaneService laneService;

    /**
     * 分页查询车道列表
     */
    @GetMapping
    @ApiOperation("分页查询车道")
    public Page<Lane> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long parkingLotId,
            @RequestParam(required = false) Integer laneType
    ) {
        return laneService.page(new Page<>(page, size));
    }

    /**
     * 根据 ID 查询车道详情
     */
    @GetMapping("/{id}")
    @ApiOperation("查询车道详情")
    public Lane getById(@PathVariable Long id) {
        return laneService.getById(id);
    }

    /**
     * 创建车道
     */
    @PostMapping
    @ApiOperation("创建车道")
    public boolean save(@RequestBody Lane lane) {
        return laneService.save(lane);
    }

    /**
     * 更新车道信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新车道")
    public boolean update(@PathVariable Long id, @RequestBody Lane lane) {
        lane.setId(id);
        return laneService.updateById(lane);
    }

    /**
     * 删除车道
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除车道")
    public boolean remove(@PathVariable Long id) {
        return laneService.removeById(id);
    }
}
