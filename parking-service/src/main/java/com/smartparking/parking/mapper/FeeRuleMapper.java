package com.smartparking.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.parking.entity.FeeRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收费规则 Mapper 接口
 */
@Mapper
public interface FeeRuleMapper extends BaseMapper<FeeRule> {
}
