package com.smartparking.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.payment.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单 Mapper 接口
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
}
