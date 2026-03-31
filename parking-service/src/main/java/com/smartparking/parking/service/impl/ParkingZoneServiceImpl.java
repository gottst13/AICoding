package com.smartparking.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartparking.common.exception.BusinessException;
import com.smartparking.parking.dto.*;
import com.smartparking.parking.entity.ParkingZone;
import com.smartparking.parking.mapper.ParkingZoneMapper;
import com.smartparking.parking.service.ParkingZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 停车场区域服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingZoneServiceImpl implements ParkingZoneService {
    
    private final ParkingZoneMapper zoneMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZoneVO createZone(Long parkingLotId, CreateZoneRequest request) {
        log.info("开始创建区域：parkingLotId={}, name={}", parkingLotId, request.getName());
        
        // 1. 验证区域层级
        if (request.getZoneType() == 2 && request.getParentZoneId() != null) {
            ParkingZone parentZone = zoneMapper.selectById(request.getParentZoneId());
            if (parentZone == null || !parentZone.getParkingLotId().equals(parkingLotId)) {
                throw new BusinessException("ZONE_002", "父区域不存在或不属于同一停车场");
            }
            // 检查父区域是否已经是子区域（不允许超过 2 级）
            if (parentZone.getZoneType() == 2) {
                throw new BusinessException("ZONE_003", "不支持超过 2 级的区域嵌套");
            }
        }
        
        // 2. 检查编码是否重复
        checkCodeDuplicate(parkingLotId, request.getCode(), null);
        
        // 3. 构建区域实体
        ParkingZone zone = ParkingZone.builder()
            .parkingLotId(parkingLotId)
            .name(request.getName())
            .code(request.getCode())
            .zoneType(request.getZoneType())
            .zoneCategory(request.getZoneCategory())
            .floorLevel(request.getFloorLevel())
            .hasIndependentExit(request.getHasIndependentExit() != null ? request.getHasIndependentExit() : false)
            .exitLaneIds(request.getExitLaneIds())
            .status(1)
            .config(request.getConfig())
            .totalSpaces(0)
            .availableSpaces(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // 如果是子区域，设置父区域 ID
        if (request.getZoneType() == 2 && request.getParentZoneId() != null) {
            zone.setParentZoneId(request.getParentZoneId());
        }
        
        // 4. 插入数据库
        zoneMapper.insert(zone);
        
        log.info("创建区域成功：id={}, name={}", zone.getId(), zone.getName());
        
        return convertToVO(zone);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ZoneVO updateZone(Long zoneId, UpdateZoneRequest request) {
        log.info("开始更新区域：zoneId={}", zoneId);
        
        // 1. 查询原区域
        ParkingZone oldZone = zoneMapper.selectById(zoneId);
        if (oldZone == null) {
            throw new BusinessException("ZONE_001", "区域不存在");
        }
        
        // 2. 如果修改了编码，检查是否重复
        if (request.getCode() != null && !request.getCode().equals(oldZone.getCode())) {
            checkCodeDuplicate(oldZone.getParkingLotId(), request.getCode(), zoneId);
        }
        
        // 3. 更新字段
        BeanUtils.copyProperties(request, oldZone);
        oldZone.setUpdatedAt(LocalDateTime.now());
        
        zoneMapper.updateById(oldZone);
        
        log.info("更新区域成功：id={}", zoneId);
        
        return convertToVO(oldZone);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteZone(Long zoneId) {
        log.info("开始删除区域：zoneId={}", zoneId);
        
        // 1. 查询区域
        ParkingZone zone = zoneMapper.selectById(zoneId);
        if (zone == null) {
            throw new BusinessException("ZONE_001", "区域不存在");
        }
        
        // 2. 如果有子区域，先删除子区域
        if (zone.getZoneType() == 1) {
            List<ParkingZone> childZones = zoneMapper.selectChildZones(zoneId);
            for (ParkingZone childZone : childZones) {
                deleteZone(childZone.getId());
            }
        }
        
        // 3. 删除区域（车位会在数据库级联删除）
        zoneMapper.deleteById(zoneId);
        
        log.info("删除区域成功：id={}", zoneId);
    }
    
    @Override
    public ZoneVO getZone(Long zoneId) {
        ParkingZone zone = zoneMapper.selectById(zoneId);
        if (zone == null) {
            throw new BusinessException("ZONE_001", "区域不存在");
        }
        return convertToVO(zone);
    }
    
    @Override
    public List<ZoneTreeVO> getZoneTree(Long parkingLotId) {
        log.info("获取停车场区域树形结构：parkingLotId={}", parkingLotId);
        
        // 1. 查询所有主区域
        LambdaQueryWrapper<ParkingZone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingZone::getParkingLotId, parkingLotId)
               .eq(ParkingZone::getZoneType, 1)  // 主区域
               .ne(ParkingZone::getStatus, 0)     // 非停用
               .orderByAsc(ParkingZone::getCode);
        
        List<ParkingZone> mainZones = zoneMapper.selectList(wrapper);
        
        // 2. 为每个主区域查询子区域
        List<ZoneTreeVO> tree = new ArrayList<>();
        for (ParkingZone mainZone : mainZones) {
            ZoneTreeVO root = convertToTreeVO(mainZone);
            
            // 查询子区域
            List<ParkingZone> childZones = zoneMapper.selectChildZones(mainZone.getId());
            List<ZoneTreeVO> children = childZones.stream()
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());
            
            root.setChildren(children);
            tree.add(root);
        }
        
        return tree;
    }
    
    // ========== 辅助方法 ==========
    
    private void checkCodeDuplicate(Long parkingLotId, String code, Long excludeId) {
        LambdaQueryWrapper<ParkingZone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingZone::getParkingLotId, parkingLotId)
               .eq(ParkingZone::getCode, code);
        if (excludeId != null) {
            wrapper.ne(ParkingZone::getId, excludeId);
        }
        
        Long count = zoneMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("ZONE_005", "区域编码已存在：" + code);
        }
    }
    
    private ZoneVO convertToVO(ParkingZone zone) {
        ZoneVO vo = new ZoneVO();
        BeanUtils.copyProperties(zone, vo);
        return vo;
    }
    
    private ZoneTreeVO convertToTreeVO(ParkingZone zone) {
        ZoneTreeVO vo = ZoneTreeVO.builder()
            .id(zone.getId())
            .name(zone.getName())
            .code(zone.getCode())
            .zoneType(zone.getZoneType())
            .floorLevel(zone.getFloorLevel())
            .availableSpaces(zone.getAvailableSpaces())
            .totalSpaces(zone.getTotalSpaces())
            .build();
        return vo;
    }
}
