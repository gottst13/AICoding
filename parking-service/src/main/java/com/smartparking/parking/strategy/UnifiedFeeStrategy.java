package com.smartparking.parking.strategy;

import com.smartparking.parking.entity.FeeRule;
import com.smartparking.parking.entity.TempOrder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * 统一计费策略 (全场统一费率)
 */
@Component
public class UnifiedFeeStrategy implements FeeStrategy {

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

        // 计算小时数 (向上取整)
        double hours = Math.ceil(durationSeconds / 3600.0);

        // 计算费用
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
        return "UNIFIED_FEE";
    }
}
