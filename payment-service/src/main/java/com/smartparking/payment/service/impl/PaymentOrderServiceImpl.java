package com.smartparking.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartparking.payment.entity.PaymentOrder;
import com.smartparking.payment.mapper.PaymentOrderMapper;
import com.smartparking.payment.service.PaymentOrderService;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现类
 */
@Service
public class PaymentOrderServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements PaymentOrderService {
}
