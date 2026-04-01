package com.smartparking.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartparking.common.exception.BusinessException;
import com.smartparking.common.page.PageResult;
import com.smartparking.parking.dto.*;
import com.smartparking.parking.entity.ParkingSpace;
import com.smartparking.parking.mapper.ParkingSpaceMapper;
import com.smartparking.parking.service.ParkingSpaceService;
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
 * 车位管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingSpaceServiceImpl implements ParkingSpaceService {
    
    private final ParkingSpaceMapper spaceMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpaceVO createSpace(Long zoneId, CreateSpaceRequest request) {
        log.info("开始创建车位：zoneId={}, spaceNo={}", zoneId, request.getSpaceNo());
        
        // 1. 检查车位编号是否重复
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSpace::getZoneId, zoneId)
               .eq(ParkingSpace::getSpaceNo, request.getSpaceNo());
        Long count = spaceMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new BusinessException("SPACE_002", "车位编号已存在");
        }
        
        // 2. 创建车位
        ParkingSpace space = ParkingSpace.builder()
            .zoneId(zoneId)
            .spaceNo(request.getSpaceNo())
            .spaceType(request.getSpaceType())
            .locationInfo(request.getLocationInfo())
            .status(1)  // 初始为空闲状态
            .widthCm(request.getWidthCm())
            .lengthCm(request.getLengthCm())
            .heightLimitCm(request.getHeightLimitCm())
            .build();
        
        int rows = spaceMapper.insert(space);
        if (rows != 1) {
            throw new BusinessException("SPACE_003", "创建车位失败");
        }
        
        log.info("车位创建成功：id={}, spaceNo={}", space.getId(), space.getSpaceNo());
        return convertToVO(space);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SpaceVO> batchCreateSpaces(Long zoneId, List<CreateSpaceRequest> requests) {
        log.info("开始批量创建车位：zoneId={}, count={}", zoneId, requests.size());
        
        List<SpaceVO> result = new ArrayList<>();
        for (CreateSpaceRequest request : requests) {
            try {
                SpaceVO vo = createSpace(zoneId, request);
                result.add(vo);
            } catch (BusinessException e) {
                log.warn("创建车位失败：spaceNo={}, error={}", request.getSpaceNo(), e.getMessage());
            }
        }
        
        log.info("批量创建车位完成：success={}, total={}", result.size(), requests.size());
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpaceStatus(Long spaceId, Integer status) {
        log.info("更新车位状态：spaceId={}, status={}", spaceId, status);
        
        ParkingSpace space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("SPACE_001", "车位不存在");
        }
        
        space.setStatus(status);
        space.setUpdatedAt(LocalDateTime.now());
        
        int rows = spaceMapper.updateById(space);
        if (rows != 1) {
            throw new BusinessException("SPACE_004", "更新车位状态失败");
        }
        
        log.info("车位状态更新成功：spaceId={}, status={}", spaceId, status);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void occupySpace(Long spaceId, String plateNo) {
        log.info("占用车位：spaceId={}, plateNo={}", spaceId, plateNo);
        
        ParkingSpace space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("SPACE_001", "车位不存在");
        }
        
        if (space.getStatus() != 1) {
            throw new BusinessException("SPACE_005", "车位不可用");
        }
        
        space.setStatus(0);  // 占用
        space.setOccupiedByPlate(plateNo);
        space.setOccupiedSince(LocalDateTime.now());
        space.setUpdatedAt(LocalDateTime.now());
        
        int rows = spaceMapper.updateById(space);
        if (rows != 1) {
            throw new BusinessException("SPACE_004", "占用车位失败");
        }
        
        log.info("车位占用成功：spaceId={}, plateNo={}", spaceId, plateNo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseSpace(Long spaceId) {
        log.info("释放车位：spaceId={}", spaceId);
        
        ParkingSpace space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("SPACE_001", "车位不存在");
        }
        
        space.setStatus(1);  // 空闲
        space.setOccupiedByPlate(null);
        space.setOccupiedSince(null);
        space.setUpdatedAt(LocalDateTime.now());
        
        int rows = spaceMapper.updateById(space);
        if (rows != 1) {
            throw new BusinessException("SPACE_006", "释放车位失败");
        }
        
        log.info("车位释放成功：spaceId={}", spaceId);
    }
    
    @Override
    public SpaceVO getSpace(Long spaceId) {
        ParkingSpace space = spaceMapper.selectById(spaceId);
        if (space == null) {
            throw new BusinessException("SPACE_001", "车位不存在");
        }
        return convertToVO(space);
    }
    
    @Override
    public List<SpaceVO> getAvailableSpaces(Long zoneId) {
        List<ParkingSpace> spaces = spaceMapper.selectAvailableSpaces(zoneId);
        return spaces.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<SpaceVO> querySpaces(SpaceQueryRequest request) {
        Page<ParkingSpace> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        if (request.getZoneId() != null) {
            wrapper.eq(ParkingSpace::getZoneId, request.getZoneId());
        }
        if (request.getSpaceType() != null) {
            wrapper.eq(ParkingSpace::getSpaceType, request.getSpaceType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(ParkingSpace::getStatus, request.getStatus());
        }
        if (request.getSpaceNo() != null && !request.getSpaceNo().isEmpty()) {
            wrapper.like(ParkingSpace::getSpaceNo, request.getSpaceNo());
        }
        
        Page<ParkingSpace> resultPage = spaceMapper.selectPage(page, wrapper);
        
        List<SpaceVO> list = resultPage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(
            (int) resultPage.getCurrent(),
            (int) resultPage.getSize(),
            resultPage.getTotal(),
            list
        );
    }
    
    /**
     * 转换为 VO
     */
    private SpaceVO convertToVO(ParkingSpace space) {
        return SpaceVO.builder()
            .id(space.getId())
            .zoneId(space.getZoneId())
            .spaceNo(space.getSpaceNo())
            .spaceType(space.getSpaceType())
            .locationInfo(space.getLocationInfo())
            .status(space.getStatus())
            .occupiedByPlate(space.getOccupiedByPlate())
            .occupiedSince(space.getOccupiedSince())
            .isCharging(space.getIsCharging())
            .chargingDeviceId(space.getChargingDeviceId())
            .widthCm(space.getWidthCm())
            .lengthCm(space.getLengthCm())
            .heightLimitCm(space.getHeightLimitCm())
            .createdAt(space.getCreatedAt())
            .updatedAt(space.getUpdatedAt())
            .build();
    }
}
