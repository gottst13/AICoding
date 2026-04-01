package com.smartparking.parking.service;

import com.smartparking.common.page.PageResult;
import com.smartparking.parking.dto.*;

import java.util.List;

/**
 * 车位管理服务接口
 */
public interface ParkingSpaceService {
    
    /**
     * 创建车位
     */
    SpaceVO createSpace(Long zoneId, CreateSpaceRequest request);
    
    /**
     * 批量创建车位
     */
    List<SpaceVO> batchCreateSpaces(Long zoneId, List<CreateSpaceRequest> requests);
    
    /**
     * 更新车位状态
     */
    void updateSpaceStatus(Long spaceId, Integer status);
    
    /**
     * 占用车位
     */
    void occupySpace(Long spaceId, String plateNo);
    
    /**
     * 释放车位
     */
    void releaseSpace(Long spaceId);
    
    /**
     * 获取车位详情
     */
    SpaceVO getSpace(Long spaceId);
    
    /**
     * 查询区域的空闲车位列表
     */
    List<SpaceVO> getAvailableSpaces(Long zoneId);
    
    /**
     * 查询车位分页列表
     */
    PageResult<SpaceVO> querySpaces(SpaceQueryRequest request);
}
