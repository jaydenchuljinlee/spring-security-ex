package com.example.security.comn.validation.user.validator;

import com.example.security.comn.validation.user.annotation.EmailFormat;
import com.example.security.comn.validation.user.checker.EmailFormatChecker;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailFormatValidator implements ConstraintValidator<EmailFormat, String> {
    private boolean nullable = false;

    @Override
    public void initialize(EmailFormat constraintAnnotation) {
        this.nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (nullable) {
            return true;
        }

        return EmailFormatChecker.check(value);
    }
}
