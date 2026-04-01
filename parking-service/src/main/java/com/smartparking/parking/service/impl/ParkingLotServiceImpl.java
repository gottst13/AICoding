package com.smartparking.parking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartparking.parking.entity.ParkingLot;
import com.smartparking.parking.mapper.ParkingLotMapper;
import com.smartparking.parking.service.ParkingLotService;
import org.springframework.stereotype.Service;

/**
 * 停车场服务实现类
 */
@Service
public class ParkingLotServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot> implements ParkingLotService {
}
