package com.zorvyn.finance.mappers;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import com.zorvyn.finance.dto.projection.CategorySummaryProjection;
import com.zorvyn.finance.dto.response.DashboardResponse;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DashboardMapper {

    public DashboardResponse.CategorySummary toCategorySummary(
            CategorySummaryProjection projection) {

        return DashboardResponse.CategorySummary.builder()
                .category(projection.getCategory())
                .total(projection.getTotal())
                .count(projection.getCount())
                .build();
    }

    public DashboardResponse.MonthlyTrend toMonthlyTrend(
            Integer year, Integer month,
            java.math.BigDecimal income,
            java.math.BigDecimal expenses) {

        String label = Month.of(month)
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                + " " + year;

        return DashboardResponse.MonthlyTrend.builder()
                .year(year)
                .month(month)
                .monthLabel(label)
                .income(income)
                .expenses(expenses)
                .net(income.subtract(expenses))
                .build();
    }
}
