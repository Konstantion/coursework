package com.konstantion.category.validator;

import com.konstantion.category.model.CreateCategoryRequest;
import com.konstantion.category.model.UpdateCategoryRequest;
import com.konstantion.utils.validator.ValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashSet;
import java.util.Set;

@Component
public record CategoryValidator(
        CategoryValidations validations
) {
    public ValidationResult validate(CreateCategoryRequest createCategoryRequest) {
        Set<FieldError> fieldErrors = new HashSet<>();
        return ValidationResult.of(fieldErrors);
    }

    public ValidationResult validate(UpdateCategoryRequest request) {
        Set<FieldError> fieldErrors = new HashSet<>();
        return ValidationResult.of(fieldErrors);
    }
}