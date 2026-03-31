package com.smartparking.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.parking.entity.ParkingZone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 停车场区域 Mapper 接口
 */
@Mapper
public interface ParkingZoneMapper extends BaseMapper<ParkingZone> {
    
    /**
     * 查询停车场的所有区域（树形结构）
     */
    List<ParkingZone> selectZoneTree(@Param("parkingLotId") Long parkingLotId);
    
    /**
     * 查询区域的所有子区域
     */
    List<ParkingZone> selectChildZones(@Param("parentZoneId") Long parentZoneId);
    
    /**
     * 更新区域可用车位数（原子操作）
     */
    int incrementAvailableSpaces(@Param("zoneId") Long zoneId, @Param("delta") Integer delta);
}
