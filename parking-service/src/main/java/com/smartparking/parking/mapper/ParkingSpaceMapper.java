package com.smartparking.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.parking.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车位 Mapper 接口
 */
@Mapper
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {
}
