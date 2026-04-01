package com.smartparking.parking.service.impl;

import com.smartparking.parking.entity.FeeRule;
import com.smartparking.parking.entity.TempOrder;
import com.smartparking.parking.mapper.FeeRuleMapper;
import com.smartparking.parking.service.FeeCalculationService;
import com.smartparking.parking.service.FeeResult;
import com.smartparking.parking.strategy.FeeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * 计费服务实现类
 */
@Slf4j
@Service
public class FeeCalculationServiceImpl implements FeeCalculationService {

    @Autowired
    private FeeRuleMapper feeRuleMapper;

    @Autowired
    private List<FeeStrategy> feeStrategies;

    @Override
    public FeeResult calculate(TempOrder order) {
        log.info("开始计算订单费用：orderNo={}", order.getOrderNo());

        // 1. 获取停车场和收费规则
        FeeRule feeRule = getMatchingFeeRule(order);
        if (feeRule == null) {
            throw new RuntimeException("未找到匹配的收费规则");
        }

        // 2. 选择合适的计费策略
        FeeStrategy strategy = selectStrategy(feeRule);

        // 3. 计算费用
        BigDecimal amount = strategy.calculate(order, feeRule);

        // 4. 构建结果
        long durationSeconds = Duration.between(order.getEnterTime(), order.getExitTime()).getSeconds();

        return FeeResult.builder()
                .originalAmount(amount)
                .discountAmount(BigDecimal.ZERO)
                .payableAmount(amount)
                .durationSeconds(durationSeconds)
                .ruleDescription(buildRuleDescription(feeRule))
                .build();
    }

    @Override
    public FeeResult tryCalculate(TempOrder order, Long parkingLotId) {
        // TODO: 实现费用试算逻辑
        return calculate(order);
    }

    /**
     * 获取匹配的收费规则
     */
    private FeeRule getMatchingFeeRule(TempOrder order) {
        // 简化处理：查询第一个激活的规则
        // 实际应该根据停车场、车型、时间等条件匹配
        return feeRuleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FeeRule>()
                .eq(FeeRule::getParkingLotId, order.getParkingLotId())
                .eq(FeeRule::getIsActive, true)
                .last("LIMIT 1"))
                .stream().findFirst().orElse(null);
    }

    /**
     * 选择计费策略
     */
    private FeeStrategy selectStrategy(FeeRule feeRule) {
        // 根据规则类型选择策略
        if (feeRule.getRuleType() == 1) {
            return feeStrategies.stream()
                    .filter(s -> "UNIFIED_FEE".equals(s.getName()))
                    .findFirst()
                    .orElse(feeStrategies.get(0));
        } else {
            return feeStrategies.stream()
                    .filter(s -> "ZONED_FEE".equals(s.getName()))
                    .findFirst()
                    .orElse(feeStrategies.get(0));
        }
    }

    /**
     * 构建规则说明
     */
    private String buildRuleDescription(FeeRule feeRule) {
        StringBuilder sb = new StringBuilder();
        sb.append("费率：").append(feeRule.getHourlyRate()).append("元/小时");
        if (feeRule.getDailyMax() != null) {
            sb.append(", 封顶：").append(feeRule.getDailyMax()).append("元");
        }
        sb.append(", 免费：").append(feeRule.getFreeMinutes()).append("分钟");
        return sb.toString();
    }
}
