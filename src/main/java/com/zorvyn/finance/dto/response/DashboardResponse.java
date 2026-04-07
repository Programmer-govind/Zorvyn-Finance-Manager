package com.zorvyn.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Long totalRecords;

    private List<CategorySummary> categoryBreakdown;
    private List<MonthlyTrend>    monthlyTrends;
    private List<RecordResponse>  recentActivity;

    @Getter
    @Builder
    public static class CategorySummary {
        private String     category;
        private BigDecimal total;
        private Long       count;
    }

    @Getter
    @Builder
    public static class MonthlyTrend {
        private Integer    year;
        private Integer    month;
        private String     monthLabel;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}