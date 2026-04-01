package com.smartparking.order.service;

import com.smartparking.order.entity.OrderSegment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单分段服务接口
 */
public interface OrderSegmentService {
    
    /**
     * 车辆进入区域（创建新分段）
     * @param orderNo 订单号
     * @param zoneId 区域 ID
     * @param zoneName 区域名称
     * @param enterTime 进入时间
     */
    void enterZone(String orderNo, Long zoneId, String zoneName, LocalDateTime enterTime);
    
    /**
     * 车辆离开区域（结束当前分段）
     * @param orderNo 订单号
     * @param zoneId 区域 ID
     * @param exitTime 离开时间
     */
    void exitZone(String orderNo, Long zoneId, LocalDateTime exitTime);
    
    /**
     * 查询订单的所有分段
     * @param orderNo 订单号
     * @return 分段列表
     */
    List<OrderSegment> getSegmentsByOrderNo(String orderNo);
    
    /**
     * 获取当前所在区域
     * @param orderNo 订单号
     * @return 当前区域 ID，如果没有活动分段则返回 null
     */
    Long getCurrentZoneId(String orderNo);
}
