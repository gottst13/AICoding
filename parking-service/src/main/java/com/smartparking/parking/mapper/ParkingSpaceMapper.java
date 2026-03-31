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
     * 查询区域的空闲车位
     */
    List<ParkingSpace> selectAvailableSpaces(@Param("zoneId") Long zoneId);
    
    /**
     * 根据车位编号查询
     */
    ParkingSpace selectBySpaceNo(@Param("zoneId") Long zoneId, @Param("spaceNo") String spaceNo);
    
    /**
     * 批量插入车位
     */
    int batchInsert(@Param("spaces") List<ParkingSpace> spaces);
}
