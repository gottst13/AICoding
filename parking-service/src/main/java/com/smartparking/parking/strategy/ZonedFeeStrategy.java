package com.smartparking.parking.strategy;

import com.smartparking.parking.entity.FeeRule;
import com.smartparking.parking.entity.TempOrder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 分区计费策略 (各区域独立费率，分段累计)
 */
@Component
public class ZonedFeeStrategy implements FeeStrategy {

    @Override
    public BigDecimal calculate(TempOrder order, FeeRule feeRule) {
        if (order.getExitTime() == null) {
            return BigDecimal.ZERO;
        }

        // 计算停车时长 (秒)
        long durationSeconds = Duration.between(order.getEnterTime(), order.getExitTime()).getSeconds();

        // 免费时长判断
        int freeMinutes = feeRule.getFreeMinutes() != null ? feeRule.getFreeMinutes() : 30;
        if (durationSeconds <= freeMinutes * 60) {
            return BigDecimal.ZERO;
        }

        // TODO: 实际场景需要根据订单分段 (order_segments) 计算每个区域的费用
        // 这里简化处理：直接使用统一费率计算
        double hours = Math.ceil(durationSeconds / 3600.0);
        BigDecimal amount = feeRule.getHourlyRate()
                .multiply(new BigDecimal(String.valueOf(hours)));

        // 封顶价保护
        if (feeRule.getDailyMax() != null && amount.compareTo(feeRule.getDailyMax()) > 0) {
            amount = feeRule.getDailyMax();
        }

        return amount;
    }

    @Override
    public String getName() {
        return "ZONED_FEE";
    }
}
