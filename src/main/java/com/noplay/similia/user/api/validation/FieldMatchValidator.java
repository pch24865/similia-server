package com.noplay.similia.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

    private String firstField;
    private String secondField;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.firstField = constraintAnnotation.first();
        this.secondField = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        Object first = new BeanWrapperImpl(value).getPropertyValue(firstField);
        Object second = new BeanWrapperImpl(value).getPropertyValue(secondField);

        if (first == null || second == null) return false;

        boolean valid = first.equals(second);

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("값이 일치하지 않습니다.")
                    .addPropertyNode(secondField)
                    .addConstraintViolation();
        }

        return valid;
    }
}