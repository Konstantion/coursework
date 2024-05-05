package com.konstantion.camp.validator;

import com.konstantion.camp.model.CreateHallRequest;
import com.konstantion.camp.model.UpdateHallRequest;
import com.konstantion.utils.validator.ValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashSet;
import java.util.Set;

@Component
public record CampValidator(
        CampValidations validations
) {
    public ValidationResult validate(CreateHallRequest request) {
        Set<FieldError> errors = new HashSet<>();
        validations.isNameValid(request.name(), request)
                .ifPresent(errors::add);
        return ValidationResult.of(errors);
    }

    public ValidationResult validate(UpdateHallRequest request) {
        Set<FieldError> errors = new HashSet<>();
        validations.isUpdateNameValid(request.name(), request)
                .ifPresent(errors::add);
        return ValidationResult.of(errors);
    }
}
