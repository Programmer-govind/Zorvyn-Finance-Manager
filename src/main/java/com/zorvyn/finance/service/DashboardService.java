package com.zorvyn.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zorvyn.finance.dto.projection.MonthlyTrendProjection;
import com.zorvyn.finance.dto.response.DashboardResponse;
import com.zorvyn.finance.dto.response.RecordResponse;
import com.zorvyn.finance.enums.TransactionType;
import com.zorvyn.finance.mappers.DashboardMapper;
import com.zorvyn.finance.mappers.RecordMapper;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import com.zorvyn.finance.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository            userRepository;

    @PreAuthorize("hasAnyRole('ROLE_ANALYST','ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public DashboardResponse getSummary() {

        // ── Totals ────────────────────────────────────────────────────
        BigDecimal totalIncome   = recordRepository.sumTotalIncome();
        BigDecimal totalExpenses = recordRepository.sumTotalExpenses();
        BigDecimal netBalance    = recordRepository.calculateNetBalance();
        Long       totalRecords  = recordRepository.countAllRecords();

        // ── Category breakdown ────────────────────────────────────────
        List<DashboardResponse.CategorySummary> categoryBreakdown =
                recordRepository.findCategoryBreakdown(null)
                        .stream()
                        .map(DashboardMapper::toCategorySummary)
                        .toList();

        // ── Monthly trends (last 12 months) ───────────────────────────
        LocalDate fromDate = LocalDate.now().minusMonths(12);
        List<MonthlyTrendProjection> rawTrends =
                recordRepository.findMonthlyTrends(fromDate);

        List<DashboardResponse.MonthlyTrend> monthlyTrends =
                buildMonthlyTrends(rawTrends);

        // ── Recent activity (last 10 records) ─────────────────────────
        List<RecordResponse> recentActivity =
                recordRepository.findRecentActivity(PageRequest.of(0, 10))
                        .stream()
                        .map(RecordMapper::toResponse)
                        .toList();

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .categoryBreakdown(categoryBreakdown)
                .monthlyTrends(monthlyTrends)
                .recentActivity(recentActivity)
                .build();
    }

    // ── Merge INCOME + EXPENSE rows into unified monthly trend ───────
    private List<DashboardResponse.MonthlyTrend> buildMonthlyTrends(
            List<MonthlyTrendProjection> rawTrends) {

        // key = "YYYY-MM"
        Map<String, BigDecimal> incomeMap  = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();

        for (MonthlyTrendProjection row : rawTrends) {
            String key = row.getYear() + "-" + row.getMonth();
            if (TransactionType.INCOME.name().equals(row.getType())) {
                incomeMap.put(key, row.getTotal());
            } else {
                expenseMap.put(key, row.getTotal());
            }
        }

        // Union of all month keys
        List<String> allKeys = new ArrayList<>();
        allKeys.addAll(incomeMap.keySet());
        expenseMap.keySet().forEach(k -> {
            if (!allKeys.contains(k)) allKeys.add(k);
        });
        allKeys.sort(String::compareTo);

        List<DashboardResponse.MonthlyTrend> result = new ArrayList<>();
        for (String key : allKeys) {
            String[] parts   = key.split("-");
            int      year    = Integer.parseInt(parts[0]);
            int      month   = Integer.parseInt(parts[1]);
            BigDecimal income   = incomeMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal expenses = expenseMap.getOrDefault(key, BigDecimal.ZERO);
            result.add(DashboardMapper.toMonthlyTrend(year, month, income, expenses));
        }
        return result;
    }
}