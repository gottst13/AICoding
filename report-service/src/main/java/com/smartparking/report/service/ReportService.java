package com.smartparking.report.service;

import com.smartparking.report.dto.FinancialReportDTO;
import com.smartparking.report.dto.OperationStatsDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 获取运营统计数据
     */
    OperationStatsDTO getOperationStats();

    /**
     * 获取财务报表 (按日期范围)
     */
    List<FinancialReportDTO> getFinancialReport(LocalDate startDate, LocalDate endDate);

    /**
     * 获取单日财务报表
     */
    FinancialReportDTO getDailyReport(LocalDate date);
}
