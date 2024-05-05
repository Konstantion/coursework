package com.konstantion.expedition.validator;

import com.konstantion.expedition.model.CreateTableRequest;
import com.konstantion.expedition.model.LoginTableRequest;
import com.konstantion.expedition.model.UpdateTableRequest;
import com.konstantion.utils.validator.ValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashSet;
import java.util.Set;

@Component
public record ExpeditionValidator(
        ExpeditionValidations validations
) {
    public ValidationResult validate(CreateTableRequest request) {
        Set<FieldError> errors = new HashSet<>();

        validations.isNameValid(request.name(), request).ifPresent(
                (errors::add)
        );

        validations.isTypeValid(request.tableType(), request).ifPresent(
                (errors::add)
        );

        validations.isPasswordValid(request.password(), request).ifPresent(
                (errors::add)
        );

        validations.isCapacityValid(request.capacity(), request).ifPresent(
                (errors::add)
        );

        return ValidationResult.of(errors);
    }

    public ValidationResult validate(LoginTableRequest request) {
        Set<FieldError> errors = new HashSet<>();

        validations.isLoginPasswordValid(request.password(), request).ifPresent(
                (errors::add)
        );

        return ValidationResult.of(errors);
    }

    public ValidationResult validate(UpdateTableRequest request) {
        Set<FieldError> errors = new HashSet<>();

        validations.isUpdateNameValid(request.name(), request).ifPresent(
                (errors::add)
        );

        validations.isUpdateTypeValid(request.tableType(), request).ifPresent(
                (errors::add)
        );

        validations.isUpdatePasswordValid(request.password(), request).ifPresent(
                (errors::add)
        );

        validations.isUpdateCapacityValid(request.capacity(), request).ifPresent(
                (errors::add)
        );

        return ValidationResult.of(errors);
    }
}
