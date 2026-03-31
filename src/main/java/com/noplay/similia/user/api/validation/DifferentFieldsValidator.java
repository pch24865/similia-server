package com.noplay.similia.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class DifferentFieldsValidator implements ConstraintValidator<DifferentFields, Object> {

    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(DifferentFields constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object first = new BeanWrapperImpl(value).getPropertyValue(firstFieldName);
        Object second = new BeanWrapperImpl(value).getPropertyValue(secondFieldName);

        if (first == null || second == null) {
            return true;
        }

        boolean valid = !first.equals(second);

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(secondFieldName)
                    .addConstraintViolation();
        }

        return valid;
    }
}