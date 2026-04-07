package com.zorvyn.finance.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class TransactionAmountValidator
        implements ConstraintValidator<ValidTransactionAmount, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value,
                           ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value.compareTo(BigDecimal.ZERO) <= 0) return false;
        return value.scale() <= 2;
    }
}
