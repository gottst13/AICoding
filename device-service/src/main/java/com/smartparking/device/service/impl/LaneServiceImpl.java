package com.smartparking.device.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartparking.device.entity.Lane;
import com.smartparking.device.mapper.LaneMapper;
import com.smartparking.device.service.LaneService;
import org.springframework.stereotype.Service;

/**
 * 车道服务实现类
 */
@Service
public class LaneServiceImpl extends ServiceImpl<LaneMapper, Lane> implements LaneService {
}
