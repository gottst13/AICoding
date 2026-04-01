package com.smartparking.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 停车订单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class ParkingOrder {
    
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;
    
    /**
     * 车场 ID
     */
    @TableField("parking_lot_id")
    private Long parkingLotId;
    
    /**
     * 车牌号
     */
    @TableField("plate_no")
    private String plateNo;
    
    /**
     * 车辆类型：1-小型车 2-大型车
     */
    @TableField("vehicle_type")
    private Integer vehicleType;
    
    /**
     * 入场时间
     */
    @TableField("enter_time")
    private LocalDateTime enterTime;
    
    /**
     * 出场时间
     */
    @TableField("exit_time")
    private LocalDateTime exitTime;
    
    /**
     * 初始区域 ID
     */
    @TableField("initial_zone_id")
    private Long initialZoneId;
    
    /**
     * 当前区域 ID
     */
    @TableField("current_zone_id")
    private Long currentZoneId;
    
    /**
     * 计费模式：1-统一计费 2-分区计费
     */
    @TableField("fee_mode")
    private Integer feeMode;
    
    /**
     * 计费规则快照（JSONB）
     */
    @TableField("fee_rule_snapshot")
    private Map<String, Object> feeRuleSnapshot;
    
    /**
     * 是否有跨区
     */
    @TableField("has_cross_zone")
    private Boolean hasCrossZone;
    
    /**
     * 跨区次数
     */
    @TableField("cross_zone_count")
    private Integer crossZoneCount;
    
    /**
     * 订单状态：0-进行中 1-已完成 2-已取消
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 支付状态：0-未支付 1-已支付
     */
    @TableField("payment_status")
    private Integer paymentStatus;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
