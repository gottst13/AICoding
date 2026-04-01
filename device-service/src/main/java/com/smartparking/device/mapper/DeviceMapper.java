package com.smartparking.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.device.entity.Device;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备 Mapper 接口
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}
