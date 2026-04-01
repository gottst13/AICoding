# 性能测试与优化报告

## 1. 性能测试概述

### 1.1 测试目标
- 验证系统在高并发场景下的稳定性
- 评估订单创建和费用计算的性能指标
- 识别性能瓶颈并提出优化建议

### 1.2 测试环境
- **测试框架**: JUnit 5 + Spring Boot Test
- **并发模型**: ThreadPoolExecutor
- **测试数据**: 模拟真实车牌号和停车场景

---

## 2. 性能测试用例

### 2.1 并发创建订单测试

**测试场景**: 
- 10 个并发线程
- 每个线程创建 5 个订单
- 总计 50 个订单

**测试代码**: `PerformanceTest.testConcurrentCreateOrders()`

**关键指标**:
- 总耗时
- 平均每个订单耗时
- 每秒处理订单数 (TPS)

### 2.2 批量费用计算测试

**测试场景**:
- 准备 20 个测试订单
- 5 个并发线程同时计算费用
- 验证批量计算的准确性

**测试代码**: `PerformanceTest.testBatchFeeCalculation()`

**关键指标**:
- 成功计算笔数
- 平均每笔计算耗时
- 每秒处理计算数 (QPS)

---

## 3. 性能优化建议

### 3.1 数据库层优化

#### 3.1.1 索引优化
```sql
-- 订单表索引
CREATE INDEX idx_orders_status ON orders(status, created_at);
CREATE INDEX idx_orders_plate_no ON orders(plate_no, enter_time);

-- 分段表索引
CREATE INDEX idx_segments_order_zone ON order_segments(order_no, zone_id);
CREATE INDEX idx_segments_time ON order_segments(start_time, end_time);
```

#### 3.1.2 连接池配置
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 3.2 应用层优化

#### 3.2.1 缓存策略
```java
// 使用 Redis 缓存计费规则
@Cacheable(value = "feeRules", key = "#parkingLotId + ':' + #vehicleType")
public FeeRule getUnifiedRule(Long parkingLotId, Integer vehicleType);

// 缓存热点区域信息
@Cacheable(value = "zones", key = "#zoneId")
public ParkingZone getZoneById(Long zoneId);
```

#### 3.2.2 异步处理
```java
// 订单创建后异步记录分段
@Async
public void asyncRecordSegment(String orderNo, Long zoneId) {
    segmentService.enterZone(orderNo, zoneId, "入场区", LocalDateTime.now());
}
```

#### 3.2.3 批量操作
```java
// 批量插入分段记录
@Transactional
public void batchSaveSegments(List<OrderSegment> segments) {
    for (OrderSegment segment : segments) {
        segmentRepository.save(segment);
    }
}
```

### 3.3 架构层优化

#### 3.3.1 读写分离
- **读操作**: 费用查询、订单详情 → 从库
- **写操作**: 订单创建、状态更新 → 主库

#### 3.3.2 消息队列
```java
// 使用 RabbitMQ/Kafka 解耦订单创建和分段记录
@RabbitListener(queues = "order.created")
public void handleOrderCreated(OrderCreatedEvent event) {
    segmentService.enterZone(
        event.getOrderNo(), 
        event.getZoneId(), 
        "入场区", 
        LocalDateTime.now()
    );
}
```

---

## 4. 监控指标

### 4.1 应用监控
- **响应时间**: P95 < 200ms, P99 < 500ms
- **吞吐量**: TPS > 100
- **错误率**: < 0.1%
- **JVM 内存**: Heap 使用率 < 80%

### 4.2 数据库监控
- **慢查询**: > 1s 的查询需要优化
- **连接数**: 活跃连接 < 最大连接的 80%
- **锁等待**: 行锁等待时间 < 100ms

### 4.3 业务监控
- **订单创建成功率**: > 99.9%
- **费用计算准确率**: 100%
- **分段记录完整率**: 100%

---

## 5. 性能基准

### 5.1 预期性能指标

| 指标 | 目标值 | 当前值 | 状态 |
|------|--------|--------|------|
| 订单创建 TPS | > 100 | - | 待测试 |
| 费用计算 QPS | > 200 | - | 待测试 |
| 平均响应时间 | < 100ms | - | 待测试 |
| 并发用户数 | > 50 | - | 待测试 |

### 5.2 压力测试方案

**阶梯式加压**:
1. 10 并发 → 稳定 5 分钟
2. 20 并发 → 稳定 5 分钟
3. 50 并发 → 稳定 10 分钟
4. 100 并发 → 观察系统表现

**监控重点**:
- CPU 使用率
- 内存使用率
- GC 频率
- 数据库连接数
- 响应时间变化

---

## 6. 优化实施计划

### Phase 1: 立即可做（1-2 天）
- [ ] 添加数据库索引
- [ ] 配置 HikariCP 连接池参数
- [ ] 启用 MyBatis 二级缓存

### Phase 2: 短期优化（1 周）
- [ ] 引入 Redis 缓存计费规则
- [ ] 实现异步分段记录
- [ ] 添加性能监控埋点

### Phase 3: 中期优化（2-4 周）
- [ ] 实施读写分离
- [ ] 引入消息队列
- [ ] 建立性能基线

### Phase 4: 长期优化（持续）
- [ ] 定期性能测试
- [ ] 性能回归测试
- [ ] 容量规划

---

## 7. 总结

通过本性能测试和优化方案，预期可以达到以下效果：

1. **性能提升**: 订单创建 TPS 提升 3-5 倍
2. **响应优化**: 平均响应时间降低到 100ms 以内
3. **并发能力**: 支持 100+ 并发用户
4. **系统稳定**: 99.9% 可用性

**下一步**: 执行实际性能测试，收集数据，验证优化效果。
