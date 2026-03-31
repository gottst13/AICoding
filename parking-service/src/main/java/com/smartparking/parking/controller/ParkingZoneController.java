package com.smartparking.parking.controller;

import com.smartparking.common.response.ApiResponse;
import com.smartparking.parking.dto.*;
import com.smartparking.parking.service.ParkingZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 停车场区域管理 Controller
 */
@RestController
@RequestMapping("/api/v1/parking-lots/{lotId}/zones")
@RequiredArgsConstructor
public class ParkingZoneController {
    
    private final ParkingZoneService zoneService;
    
    /**
     * 获取停车场区域树形结构
     */
    @GetMapping("/tree")
    public ApiResponse<List<ZoneTreeVO>> getZoneTree(@PathVariable Long lotId) {
        List<ZoneTreeVO> tree = zoneService.getZoneTree(lotId);
        return ApiResponse.success(tree);
    }
    
    /**
     * 创建区域
     */
    @PostMapping
    public ApiResponse<ZoneVO> createZone(
        @PathVariable Long lotId,
        @Validated @RequestBody CreateZoneRequest request
    ) {
        ZoneVO zone = zoneService.createZone(lotId, request);
        return ApiResponse.success(zone);
    }
    
    /**
     * 获取区域详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ZoneVO> getZone(@PathVariable Long id) {
        ZoneVO zone = zoneService.getZone(id);
        return ApiResponse.success(zone);
    }
    
    /**
     * 更新区域
     */
    @PutMapping("/{id}")
    public ApiResponse<ZoneVO> updateZone(
        @PathVariable Long id,
        @Validated @RequestBody UpdateZoneRequest request
    ) {
        ZoneVO zone = zoneService.updateZone(id, request);
        return ApiResponse.success(zone);
    }
    
    /**
     * 删除区域（级联删除子区域和车位）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ApiResponse.success();
    }
}
