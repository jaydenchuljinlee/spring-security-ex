package com.example.security.comn.validation.user.annotation;

import com.example.security.comn.validation.user.validator.UserNameValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {UserNameValidator.class})
public @interface UserNameForamt {
    String message() default "UserName validation error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean nullable() default false;
}
