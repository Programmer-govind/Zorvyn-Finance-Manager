package com.zorvyn.finance.dto.projection;

import java.math.BigDecimal;

//Used by dashboard: monthly income vs expense trend

public interface MonthlyTrendProjection {
    Integer getYear();
    Integer getMonth();
    String getType();
    BigDecimal getTotal();
}
