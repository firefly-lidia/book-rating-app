package com.lp.book.rating.app.controller.book.annotation;

import com.lp.book.rating.app.controller.book.dto.PatchBookRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MoneyConsistencyValidator implements ConstraintValidator<MoneyConsistency, PatchBookRequest> {

    @Override
    public boolean isValid(PatchBookRequest p, ConstraintValidatorContext ctx) {
        boolean hasPrice = p.price().isPresent();
        boolean hasCurrency = p.currency().isPresent();

        if (hasPrice && !hasCurrency) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("currency must be provided when updating monetary fields")
                .addPropertyNode("currency").addConstraintViolation();
            return false;
        }

        return true;
    }

}
