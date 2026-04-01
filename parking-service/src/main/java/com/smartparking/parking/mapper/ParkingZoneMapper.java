package com.smartparking.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.parking.entity.ParkingZone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 停车区域 Mapper 接口
 */
@Mapper
public interface ParkingZoneMapper extends BaseMapper<ParkingZone> {
    
    /**
     * 查询子区域列表
     */
    List<ParkingZone> selectChildZones(@Param("parentZoneId") Long parentZoneId);
}
