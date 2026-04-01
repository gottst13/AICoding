package com.smartparking.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.device.entity.Lane;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车道 Mapper 接口
 */
@Mapper
public interface LaneMapper extends BaseMapper<Lane> {
}
