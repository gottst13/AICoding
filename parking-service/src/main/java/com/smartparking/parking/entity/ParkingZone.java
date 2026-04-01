package com.smartparking.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停车区域实体类
 * 
 * @author Smart Parking Team
 * @since 1.0.0
 */
@Data
@TableName("parking_zones")
public class ParkingZone {

    /**
     * 区域 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 停车场 ID
     */
    private Long parkingLotId;

    /**
     * 父区域 ID (支持嵌套区域)
     */
    private Long parentZoneId;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 区域编码
     */
    private String code;

    /**
     * 区域类型：1-一级区域 2-子区域
     */
    private Integer zoneType;

    /**
     * 区域类别：1-商场区 2-办公区 3-住宅区 4-充电区 5-其他
     */
    private Integer zoneCategory;

    /**
     * 楼层 (-1 表示地下一层)
     */
    private Integer floorLevel;

    /**
     * 总车位数
     */
    private Integer totalSpaces;

    /**
     * 可用剩余车位数
     */
    private Integer availableSpaces;

    /**
     * 是否有独立出口
     */
    private Boolean hasIndependentExit;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
