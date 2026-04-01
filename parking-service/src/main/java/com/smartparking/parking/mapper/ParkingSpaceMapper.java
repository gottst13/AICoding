package com.smartparking.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.parking.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车位 Mapper 接口
 */
@Mapper
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {
    
    /**
     * 查询区域的空闲车位列表
     */
    List<ParkingSpace> selectAvailableSpaces(@Param("zoneId") Long zoneId);
}
