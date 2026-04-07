package com.zorvyn.finance.validators;

import com.zorvyn.finance.enums.TransactionType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransactionTypeValidator
     implements ConstraintValidator<ValidTransactionType, String> {

 @Override
 public boolean isValid(String value,
                        ConstraintValidatorContext context) {
     if (value == null) return true;
     try {
         TransactionType.valueOf(value.toUpperCase());
         return true;
     } catch (IllegalArgumentException e) {
         return false;
     }
 }
}