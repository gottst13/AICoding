package com.smartparking.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.order.entity.FeeRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 计费规则 Mapper 接口
 */
@Mapper
public interface FeeRuleMapper extends BaseMapper<FeeRule> {
    
    /**
     * 查询车场的统一计费规则
     */
    FeeRule selectUnifiedRule(
        @Param("parkingLotId") Long parkingLotId,
        @Param("vehicleType") Integer vehicleType,
        @Param("currentTime") LocalDateTime currentTime
    );
    
    /**
     * 查询区域的分区计费规则
     */
    List<FeeRule> selectZoneRules(
        @Param("parkingLotId") Long parkingLotId,
        @Param("zoneId") Long zoneId,
        @Param("vehicleType") Integer vehicleType
    );
}
