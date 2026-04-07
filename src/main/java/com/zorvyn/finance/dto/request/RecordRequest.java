package com.zorvyn.finance.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.zorvyn.finance.enums.TransactionType;
import com.zorvyn.finance.validators.ValidTransactionAmount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordRequest {

    @NotNull(message = "Amount is required")
    @ValidTransactionAmount
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}