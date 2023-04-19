package com.konstantion.product.validator;

import com.konstantion.product.Product;
import com.konstantion.product.model.CreateProductRequest;
import com.konstantion.product.model.UpdateProductRequest;
import com.konstantion.utils.validator.ValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashSet;
import java.util.Set;

@Component
public record ProductValidator(
        ProductValidations productValidations
) {
    public ValidationResult validate(CreateProductRequest request) {
        Set<FieldError> errors = new HashSet<>();

        productValidations.isNameValid(request.name(), request)
                .ifPresent(errors::add);

        productValidations.isPriceValid(request.price(), request)
                .ifPresent(errors::add);

        productValidations.isDescriptionValid(request.description(), request)
                .ifPresent(errors::add);

        productValidations.isWeightValid(request.weight(), request)
                .ifPresent(errors::add);

        return ValidationResult.of(errors);
    }

    public ValidationResult validate(UpdateProductRequest request) {
        Set<FieldError> errors = new HashSet<>();

        productValidations.isUpdateNameValid(request.name(), request)
                .ifPresent(errors::add);

        productValidations.isUpdatePriceValid(request.price(), request)
                .ifPresent(errors::add);

        productValidations.isUpdateDescriptionValid(request.description(), request)
                .ifPresent(errors::add);

        productValidations.isUpdateWeightValid(request.weight(), request)
                .ifPresent(errors::add);

        return ValidationResult.of(errors);
    }
}
