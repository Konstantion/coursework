package edu.geekhub.utils.validation;

import static edu.geekhub.utils.validation.messages.ValidationMessagesGenerator.*;
import static edu.geekhub.utils.validation.messages.ValidationParameter.SPECIFIC_CHARACTERS;
import static edu.geekhub.utils.validation.patterns.PatternsEnum.*;

import edu.geekhub.exceptions.UserValidationException;
import edu.geekhub.models.User;
import edu.geekhub.utils.validation.messages.ValidationParameter;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public interface Validatable {

    default boolean isNotNull(Object object, ValidationParameter parameter)
            throws UserValidationException {
        if (Objects.isNull(object)) {
            throw new UserValidationException(cannotBeNull(parameter));
        } else {
            return true;
        }
    }

    default boolean isNotEmpty(String string, ValidationParameter parameter)
            throws UserValidationException {
        if (string.isEmpty()) {
            throw new UserValidationException(cannotBeEmpty(parameter));
        } else {
            return true;
        }
    }

    boolean isIdUnique(UUID id, User[] users, ValidationParameter parameter);

    boolean isEmailUnique(String email, User[] users, ValidationParameter parameter);

    boolean isUsernameUnique(String username, User[] users, ValidationParameter parameter);

    boolean isEmailValid(String email, ValidationParameter parameter);

    default boolean isWithoutCharacters(String input, ValidationParameter parameter)
            throws UserValidationException {
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS.getPattern());
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            throw new UserValidationException(cannotContainSpecialCharacters(
                    parameter, SPECIFIC_CHARACTERS)
            );
        } else {
            return true;
        }
    }

    default boolean isWithoutSpaces(String input, ValidationParameter parameter)
            throws UserValidationException {
        if (input.contains(" ")) {
            throw new UserValidationException(cannotContainSpaces(parameter));
        } else {
            return true;
        }
    }

    default boolean isOneWord(String input, ValidationParameter parameter)
            throws UserValidationException {
        Pattern pattern = Pattern.compile(ONE_WORD.getPattern());
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new UserValidationException(mustOneWord(parameter));
        } else {
            return true;
        }
    }

    default boolean isOnlyLetters(String input, ValidationParameter parameter)
            throws UserValidationException {
        Pattern pattern = Pattern.compile(ONLY_LETTERS.getPattern());
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new UserValidationException(mustContainOnlyLetters(parameter));
        } else {
            return true;
        }
    }

    default boolean isInLowercase(String input, ValidationParameter parameter)
            throws UserValidationException {
        Pattern pattern = Pattern.compile(LOWERCASE.getPattern());
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new UserValidationException(mustBeInLowercase(parameter));
        } else {
            return true;
        }
    }

    default boolean isCamelCase(String input, ValidationParameter parameter)
            throws UserValidationException {
        Pattern pattern = Pattern.compile(CAMEL_CASE.getPattern());
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new UserValidationException(mustBeWrittenInCamelCase(parameter));
        } else {
            return true;
        }
    }

    default boolean isTwoWordSeparatedBySpace(String input, ValidationParameter parameter)
            throws UserValidationException {
        Pattern pattern = Pattern.compile(TWO_WORDS_SEPARATED_BY_SPACE.getPattern());
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new UserValidationException(mustBeTwoWordsSeparatedBySpace(parameter));
        } else {
            return true;
        }
    }

    default boolean isOverThan(Integer input, Integer than, ValidationParameter parameter)
            throws UserValidationException {
        if (input < than) {
            throw new UserValidationException(mustBeOver(parameter, than.toString()));
        } else {
            return true;
        }
    }

    default boolean isLessThan(Integer input, Integer than, ValidationParameter parameter)
            throws UserValidationException {
        if (input > than) {
            throw new UserValidationException(mustBeLess(parameter, than.toString()));
        } else {
            return true;
        }
    }

    default boolean isLonerThan(String input, Integer than, ValidationParameter parameter)
            throws UserValidationException {
        if (input.length() > than) {
            throw new UserValidationException(cannotBeLonger(parameter, than.toString()));
        } else {
            return true;
        }
    }

    default boolean isZeroOrBigger(Long input, ValidationParameter parameter)
            throws UserValidationException {
        if (input < 0) {
            throw new UserValidationException(mustBeZeroOrBigger(parameter));
        } else {
            return true;
        }
    }
}