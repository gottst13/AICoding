package com.smartparking.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.order.entity.ParkingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 停车订单 Mapper
 */
@Mapper
public interface ParkingOrderMapper extends BaseMapper<ParkingOrder> {
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    ParkingOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
