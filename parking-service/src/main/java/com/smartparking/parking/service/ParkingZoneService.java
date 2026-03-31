package com.smartparking.parking.service;

import com.smartparking.parking.dto.*;

import java.util.List;

/**
 * 停车场区域服务接口
 */
public interface ParkingZoneService {
    
    /**
     * 创建区域
     */
    ZoneVO createZone(Long parkingLotId, CreateZoneRequest request);
    
    /**
     * 更新区域
     */
    ZoneVO updateZone(Long zoneId, UpdateZoneRequest request);
    
    /**
     * 删除区域（级联删除子区域和车位）
     */
    void deleteZone(Long zoneId);
    
    /**
     * 获取区域详情
     */
    ZoneVO getZone(Long zoneId);
    
    /**
     * 获取停车场区域树形结构
     */
    List<ZoneTreeVO> getZoneTree(Long parkingLotId);
}
