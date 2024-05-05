package com.konstantion.gear.validator;

import com.konstantion.utils.validator.ValidationUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.konstantion.utils.validator.ValidationConstants.DESCRIPTION_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.NAME_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.PRICE_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.WEIGHT_FIELD;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public record GearValidator(

) implements ValidationUtil {
    private static final double MAX_PRICE = 0x10000;

    public Optional<FieldError> isNameValid(String name, Object sender) {
        Set<String> violations = new HashSet<>();
        if (isBlank(name)) {
            violations.add("name shouldn't be empty");
            return setToOptional(violations, sender, NAME_FIELD);
        }

        if (name.length() > 32) {
            violations.add("name shouldn't be longer then 32 symbols");
        }

        return setToOptional(violations, sender, NAME_FIELD);
    }

    public Optional<FieldError> isPriceValid(Double price, Object sender) {
        Set<String> violations = new HashSet<>();
        if (isNull(price)) {
            violations.add("price shouldn't be null");
            return setToOptional(violations, sender, PRICE_FIELD);
        }

        if (price <= 0) {
            violations.add("price should be bigger then zero");
        }

        if (price > MAX_PRICE) {
            violations.add(format("price should not be bigger %s", MAX_PRICE));
        }

        BigDecimal priceDecimal = BigDecimal.valueOf(price);
        if (priceDecimal.scale() > 2) {
            violations.add("price should have at most two decimal digits");
        }

        return setToOptional(violations, sender, PRICE_FIELD);
    }

    public Optional<FieldError> isDescriptionValid(String description, Object sender) {
        Set<String> violations = new HashSet<>();
        if (isNull(description)) {
            return Optional.empty();
        }

        if (description.length() > 256) {
            violations.add("description shouldn't be longer then 256 symbols");
        }

        return setToOptional(violations, sender, DESCRIPTION_FIELD);
    }

    public Optional<FieldError> isWeightValid(Double weight, Object sender) {
        Set<String> violations = new HashSet<>();
        if (isNull(weight)) {
            return Optional.empty();
        }

        if (weight <= 0) {
            violations.add("weight should be bigger than 0");
        }

        return setToOptional(violations, sender, WEIGHT_FIELD);
    }

    public Optional<FieldError> isUpdateNameValid(String name, Object sender) {
        if (isNull(name)) {
            return Optional.empty();
        }
        return isNameValid(name, sender);
    }

    public Optional<FieldError> isUpdatePriceValid(Double price, Object sender) {
        if (isNull(price)) {
            return Optional.empty();
        }
        return isPriceValid(price, sender);
    }

    public Optional<FieldError> isUpdateDescriptionValid(String description, Object sender) {
        if (isNull(description)) {
            return Optional.empty();
        }
        return isDescriptionValid(description, sender);
    }

    public Optional<FieldError> isUpdateWeightValid(Double weight, Object sender) {
        if (isNull(weight)) {
            return Optional.empty();
        }
        return isWeightValid(weight, sender);
    }
}
