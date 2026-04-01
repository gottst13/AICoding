package com.smartparking.order.performance;

import com.smartparking.order.entity.ParkingOrder;
import com.smartparking.order.service.ParkingOrderService;
import com.smartparking.order.service.OrderSegmentService;
import com.smartparking.order.dto.FeeCalculationVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能测试 - 并发场景
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class PerformanceTest {
    
    @Autowired
    private ParkingOrderService orderService;
    
    @Autowired
    private OrderSegmentService segmentService;
    
    /**
     * 测试并发创建订单的性能
     */
    @Test
    @DisplayName("并发创建订单性能测试")
    void testConcurrentCreateOrders() throws Exception {
        log.info("========== 开始并发创建订单性能测试 ==========");
        
        int threadCount = 10;
        int ordersPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        // 提交并发任务
        for (int i = 0; i < threadCount; i++) {
            final int threadNo = i;
            executor.submit(() -> {
                try {
                    long threadStart = System.currentTimeMillis();
                    
                    for (int j = 0; j < ordersPerThread; j++) {
                        String plateNo = String.format("京 A%05d", threadNo * ordersPerThread + j);
                        ParkingOrder order = orderService.createOrder(plateNo, 1L, 1L);
                        if (order != null) {
                            successCount.incrementAndGet();
                        }
                    }
                    
                    long threadEnd = System.currentTimeMillis();
                    totalTime.addAndGet(threadEnd - threadStart);
                    log.info("线程{}完成，创建{}个订单，耗时{}ms", 
                        threadNo, ordersPerThread, (threadEnd - threadStart));
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有任务完成
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        
        log.info("========== 性能测试结果 ==========");
        log.info("总订单数：{}", successCount.get());
        log.info("总耗时：{}ms", totalDuration);
        log.info("平均每个订单耗时：{}ms", totalDuration / successCount.get());
        log.info("每秒处理订单数：{:.2f}", successCount.get() * 1000.0 / totalDuration);
        log.info("==================================");
        
        assertTrue(successCount.get() > 0, "至少有一个订单创建成功");
    }
    
    /**
     * 测试批量费用计算的性能
     */
    @Test
    @DisplayName("批量费用计算性能测试")
    void testBatchFeeCalculation() throws Exception {
        log.info("========== 开始批量费用计算性能测试 ==========");
        
        int batchSize = 20;
        List<ParkingOrder> orders = new ArrayList<>();
        
        // 准备测试数据
        for (int i = 0; i < batchSize; i++) {
            String plateNo = String.format("京 B%05d", i);
            ParkingOrder order = orderService.createOrder(plateNo, 1L, 1L);
            orders.add(order);
        }
        
        log.info("已创建{}个测试订单", batchSize);
        
        // 并发计算费用
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(batchSize);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        for (ParkingOrder order : orders) {
            executor.submit(() -> {
                try {
                    long calcStart = System.currentTimeMillis();
                    
                    FeeCalculationVO result = orderService.calculateOrderFee(order.getOrderNo());
                    
                    long calcEnd = System.currentTimeMillis();
                    totalTime.addAndGet(calcEnd - calcStart);
                    
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        
        log.info("========== 性能测试结果 ==========");
        log.info("成功计算：{}笔", successCount.get());
        log.info("总耗时：{}ms", totalDuration);
        log.info("平均每笔计算耗时：{}ms", totalDuration / successCount.get());
        log.info("每秒处理计算：{:.2f}", successCount.get() * 1000.0 / totalDuration);
        log.info("==================================");
        
        assertTrue(successCount.get() >= batchSize * 0.9, "90% 以上的计算成功");
    }
}
