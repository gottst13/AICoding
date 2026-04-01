package com.smartparking.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartparking.order.entity.TempOrder;
import com.smartparking.order.mapper.TempOrderMapper;
import com.smartparking.order.service.TempOrderService;
import org.springframework.stereotype.Service;

/**
 * 订单服务实现类
 */
@Service
public class TempOrderServiceImpl extends ServiceImpl<TempOrderMapper, TempOrder> implements TempOrderService {
}
