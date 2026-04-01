package com.smartparking.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.order.entity.OrderSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单分段 Mapper
 */
@Mapper
public interface OrderSegmentMapper extends BaseMapper<OrderSegment> {
    
    /**
     * 根据订单号查询所有分段
     * @param orderNo 订单号
     * @return 分段列表
     */
    List<OrderSegment> findByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 查询订单的最后一个分段
     * @param orderNo 订单号
     * @return 最后一段
     */
    OrderSegment findLastSegment(@Param("orderNo") String orderNo);
    
    /**
     * 根据订单号和区域 ID 查询分段
     * @param orderNo 订单号
     * @param zoneId 区域 ID
     * @return 分段列表
     */
    List<OrderSegment> findByOrderNoAndZoneId(
        @Param("orderNo") String orderNo, 
        @Param("zoneId") Long zoneId
    );
}
