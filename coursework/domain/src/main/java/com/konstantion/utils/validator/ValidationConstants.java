package com.konstantion.utils.validator;

public record ValidationConstants() {
    public static final String EMAIL_UNICODE_CHARACTERS_REGEX = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
            + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$";
    public static final String PHONE_NUMBER_PATTERN = "^\\d{3}-\\d{3}-\\d{4}$";
    public static final String FIRST_NAME_FIELD = "firstName";
    public static final String LAST_NAME_FIELD = "lastName";
    public static final String EMAIL_FIELD = "email";
    public static final String PHONE_NUMBER_FIELD = "phoneNumber";
    public static final String AGE_FIELD = "age";
    public static final String PASSWORD_FIELD = "password";
    public static final String PASSWORD_COPY_FIELD = "passwordCopy";
    public static final String NAME_FIELD = "name";
    public static final String DISCOUNT_PERCENT_FIELD = "discountPercent";
    public static final String CAPACITY_FIELD = "capacity";
    public static final String TABLE_TYPE_FIELD = "tableType";
    public static final String PRICE_FIELD = "price";
    public static final String WEIGHT_FIELD = "weight";
    public static final String DESCRIPTION_FIELD = "description";
}
