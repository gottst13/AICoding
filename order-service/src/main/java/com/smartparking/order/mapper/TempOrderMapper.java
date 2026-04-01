package com.smartparking.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.order.entity.TempOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface TempOrderMapper extends BaseMapper<TempOrder> {
}
