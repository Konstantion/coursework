package com.konstantion.user.validator;

import com.konstantion.utils.validator.ValidationUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static com.konstantion.utils.validator.ValidationConstants.AGE_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.EMAIL_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.EMAIL_UNICODE_CHARACTERS_REGEX;
import static com.konstantion.utils.validator.ValidationConstants.FIRST_NAME_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.LAST_NAME_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.PASSWORD_COPY_FIELD;
import static com.konstantion.utils.validator.ValidationConstants.PASSWORD_FIELD;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public record UserValidations(

) implements ValidationUtil {
    public Optional<FieldError> isFirstNameValid(String firstName, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isBlank(firstName)) {
            violations.add("first name shouldn't be empty");
            return setToOptional(violations, sender, FIRST_NAME_FIELD);
        }

        if (!isAlpha(firstName)) {
            violations.add("first name should contain only alphabetic characters");
        }

        if (firstName.length() > 32) {
            violations.add("first name shouldn't be longer then 32 symbols");
        }

        return setToOptional(violations, sender, FIRST_NAME_FIELD);
    }

    public Optional<FieldError> isLastNameValid(String firstName, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isBlank(firstName)) {
            violations.add("last name shouldn't be empty");
            return setToOptional(violations, sender, LAST_NAME_FIELD);
        }

        if (!isAlpha(firstName)) {
            violations.add("last name should contain only alphabetic characters");
        }

        if (firstName.length() > 32) {
            violations.add("last name shouldn't be longer then 32 symbols");
        }

        return setToOptional(violations, sender, LAST_NAME_FIELD);
    }

    public Optional<FieldError> isEmailValid(String email, Object sender) {
        Set<String> violations = new HashSet<>();
        if (isBlank(email)) {
            violations.add("email shouldn't be empty");
            return setToOptional(violations, sender, EMAIL_FIELD);
        }

        if (!Pattern.matches(EMAIL_UNICODE_CHARACTERS_REGEX, email)) {
            violations.add("email should match pattern <example@mail.com>");
            return setToOptional(violations, sender, EMAIL_FIELD);
        }

        return setToOptional(violations, sender, EMAIL_FIELD);
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

    public Optional<FieldError> isPasswordCopyValid(String password, String passwordCopy, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isBlank(passwordCopy)) {
            violations.add("password copy shouldn't be empty");
            return setToOptional(violations, sender, PASSWORD_COPY_FIELD);
        }


        if (!password.equals(passwordCopy)) {
            violations.add("password copy should be the same as password");
        }

        return setToOptional(violations, sender, PASSWORD_COPY_FIELD);
    }

    public Optional<FieldError> isAgeValid(Integer age, Object sender) {
        Set<String> violations = new HashSet<>();

        if (isNull(age)) {
            return Optional.empty();
        }

        if (age < 14 || age >= 100) {
            violations.add("age should be between 14 and 100");
        }

        return setToOptional(violations, sender, AGE_FIELD);
    }

    public Optional<FieldError> isUpdateFirstNameValid(String firstName, Object sender) {
        if (isNull(firstName)) {
            return Optional.empty();
        }
        return isFirstNameValid(firstName, sender);
    }

    public Optional<FieldError> isUpdateLastNameValid(String lastName, Object sender) {
        if (isNull(lastName)) {
            return Optional.empty();
        }
        return isLastNameValid(lastName, sender);
    }

    public Optional<FieldError> isUpdateEmailValid(String email, Object sender) {
        if (isNull(email)) {
            return Optional.empty();
        }
        return isEmailValid(email, sender);
    }

    public Optional<FieldError> isUpdatePhoneNumberValid(String phoneNumber, Object sender) {
        if (isNull(phoneNumber)) {
            return Optional.empty();
        }
        return isPhoneNumberValid(phoneNumber, sender);
    }

    public Optional<FieldError> isUpdateAgeValid(Integer age, Object sender) {
        if (isNull(age)) {
            return Optional.empty();
        }
        return isAgeValid(age, sender);
    }

    public Optional<FieldError> isUpdatePasswordValid(String password, Object sender) {
        if (isNull(password)) {
            return Optional.empty();
        }
        return isPasswordValid(password, sender);
    }


    public Optional<FieldError> isAdminEmailValid(String email, Object sender) {
        Set<String> violations = new HashSet<>();
        if (isBlank(email)) {
            violations.add("email shouldn't be empty");
            return setToOptional(violations, sender, EMAIL_FIELD);
        }

        return setToOptional(violations, sender, EMAIL_FIELD);
    }
}
