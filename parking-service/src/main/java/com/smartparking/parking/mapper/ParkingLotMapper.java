package com.smartparking.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.parking.entity.ParkingLot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 停车场 Mapper 接口
 */
@Mapper
public interface ParkingLotMapper extends BaseMapper<ParkingLot> {
}
