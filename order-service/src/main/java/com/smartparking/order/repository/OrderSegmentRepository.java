package com.smartparking.order.repository;

import com.smartparking.order.entity.OrderSegment;
import com.smartparking.order.mapper.OrderSegmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单分段 Repository
 */
@Repository
@RequiredArgsConstructor
public class OrderSegmentRepository {
    
    private final OrderSegmentMapper segmentMapper;
    
    /**
     * 根据订单号查询所有分段
     */
    public List<OrderSegment> findByOrderNo(String orderNo) {
        return segmentMapper.findByOrderNo(orderNo);
    }
    
    /**
     * 查询订单的最后一个分段
     */
    public OrderSegment findLastSegment(String orderNo) {
        return segmentMapper.findLastSegment(orderNo);
    }
    
    /**
     * 保存分段
     */
    public int save(OrderSegment segment) {
        return segmentMapper.insert(segment);
    }
    
    /**
     * 更新分段
     */
    public int update(OrderSegment segment) {
        return segmentMapper.updateById(segment);
    }
}
