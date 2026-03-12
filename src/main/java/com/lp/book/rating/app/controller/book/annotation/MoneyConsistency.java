package com.lp.book.rating.app.controller.book.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MoneyConsistencyValidator.class)
public @interface MoneyConsistency {
    String message() default "Money fields inconsistent";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

