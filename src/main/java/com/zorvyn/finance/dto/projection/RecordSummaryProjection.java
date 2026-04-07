package com.zorvyn.finance.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

//Used by record listing: lightweight view

public interface RecordSummaryProjection {
    Long getId();
    BigDecimal getAmount();
    String getType();
    String getCategory();
    LocalDate getTransactionDate();
    String getNotes();
}
