package com.konstantion.guest.validator;

import com.konstantion.utils.validator.ValidationUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.konstantion.utils.validator.ValidationConstants.DISCOUNT_PERCENT_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.NAME_FIELD;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public record GuestValidations(

) implements ValidationUtil {
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

    public Optional<FieldError> isDiscountValid(Double discount, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isNull(discount)) {
            violations.add("discount shouldn't be null");
            return setToOptional(violations, sender, DISCOUNT_PERCENT_FIELD);
        }

        if (discount < 0 || discount > 100) {
            violations.add("discount should be between 1 and 100");
        }

        return setToOptional(violations, sender, DISCOUNT_PERCENT_FIELD);
    }

    public Optional<FieldError> isUpdateNameValid(String name, Object sender) {
        if (isNull(name)) {
            return Optional.empty();
        }

        return isNameValid(name, sender);
    }

    public Optional<FieldError> isUpdatePhoneNumberValid(String phoneNumber, Object sender) {
        if (isNull(phoneNumber)) {
            return Optional.empty();
        }

        return isPhoneNumberValid(phoneNumber, sender);
    }

    public Optional<FieldError> isUpdateDiscountValid(Double discount, Object sender) {
        if (isNull(discount)) {
            return Optional.empty();
        }

        return isDiscountValid(discount, sender);
    }
}
