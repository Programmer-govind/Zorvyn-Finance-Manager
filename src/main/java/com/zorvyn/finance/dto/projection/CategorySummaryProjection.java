package com.zorvyn.finance.dto.projection;

import java.math.BigDecimal;

//Used by dashboard: category-wise totals

public interface CategorySummaryProjection {
    String getCategory();
    BigDecimal getTotal();
    Long getCount();
}
