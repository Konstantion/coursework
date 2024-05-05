package com.konstantion.expedition.validator;

import com.konstantion.expedition.ExpeditionType;
import com.konstantion.utils.validator.ValidationUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.konstantion.utils.validator.ValidationConstants.CAPACITY_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.NAME_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.PASSWORD_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.TABLE_TYPE_FIELD;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public record ExpeditionValidations() implements ValidationUtil {

    public Optional<FieldError> isNameValid(String name, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isBlank(name)) {
            violations.add("name shouldn't be empty");
            return setToOptional(violations, sender, NAME_FIELD);
        }

        if (name.length() > 32) {
            violations.add("name max length is 32 characters");
        }

        return setToOptional(violations, sender, NAME_FIELD);
    }

    public Optional<FieldError> isCapacityValid(Integer capacity, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isNull(capacity)) {
            violations.add("capacity shouldn't be empty");
            return setToOptional(violations, sender, CAPACITY_FIELD);
        }

        if (capacity <= 0 || capacity >= 13) {
            violations.add("capacity should be between 1 and 12");
        }

        return setToOptional(violations, sender, CAPACITY_FIELD);
    }

    public Optional<FieldError> isTypeValid(String tableType, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isBlank(tableType)) {
            violations.add("table type shouldn't be empty");
            return setToOptional(violations, sender, NAME_FIELD);
        }

        try {
            ExpeditionType.valueOf(tableType);
        } catch (IllegalArgumentException e) {
            violations.add(format("no table type %s exist", tableType));
        }

        return setToOptional(violations, sender, TABLE_TYPE_FIELD);
    }

    public Optional<FieldError> isPasswordValid(String password, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isBlank(password)) {
            violations.add("password shouldn't be empty");
            return setToOptional(violations, sender, PASSWORD_FIELD);
        }

        if (password.length() < 4 || password.length() > 20) {
            violations.add("password should be between 4 and 20 characters");
        }


        return setToOptional(violations, sender, PASSWORD_FIELD);
    }

    public Optional<FieldError> isLoginPasswordValid(String password, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isBlank(password)) {
            violations.add("password shouldn't be empty");
            return setToOptional(violations, sender, PASSWORD_FIELD);
        }

        return setToOptional(violations, sender, PASSWORD_FIELD);
    }

    public Optional<FieldError> isUpdateNameValid(String name, Object sender) {
        if (isNull(name)) {
            return Optional.empty();
        }
        return isNameValid(name, sender);
    }

    public Optional<FieldError> isUpdateCapacityValid(Integer capacity, Object sender) {
        if (isNull(capacity)) {
            return Optional.empty();
        }
        return isCapacityValid(capacity, sender);
    }

    public Optional<FieldError> isUpdateTypeValid(String tableType, Object sender) {
        return isTypeValid(tableType, sender);
    }

    public Optional<FieldError> isUpdatePasswordValid(String password, Object sender) {
        if (isNull(password)) {
            return Optional.empty();
        }
        return isPasswordValid(password, sender);
    }
}
