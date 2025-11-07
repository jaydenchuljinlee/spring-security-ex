package com.example.security.comn.validation.user.annotation;

import com.example.security.comn.validation.user.validator.EmailFormatValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
@Documented
@Constraint(validatedBy = {EmailFormatValidator.class})
public @interface EmailFormat {
    String message() default "Email validation error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean nullable() default false;
}
