package com.smartparking.report.controller;

import com.smartparking.report.dto.FinancialReportDTO;
import com.smartparking.report.dto.OperationStatsDTO;
import com.smartparking.report.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@Api(tags = "报表统计接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取运营统计数据
     */
    @GetMapping("/operation-stats")
    @ApiOperation("获取运营统计")
    public OperationStatsDTO getOperationStats() {
        return reportService.getOperationStats();
    }

    /**
     * 获取财务报表 (日期范围)
     */
    @GetMapping("/financial")
    @ApiOperation("获取财务报表")
    public List<FinancialReportDTO> getFinancialReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return reportService.getFinancialReport(startDate, endDate);
    }

    /**
     * 获取单日财务报表
     */
    @GetMapping("/financial/daily")
    @ApiOperation("获取单日财务报表")
    public FinancialReportDTO getDailyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        return reportService.getDailyReport(date);
    }
}
