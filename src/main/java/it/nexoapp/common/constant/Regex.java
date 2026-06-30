package it.nexoapp.common.constant;

import it.nexoapp.common.exception.IllegalInstantiationException;

/**
 * The RegexValidation class provides a collection of predefined regular expressions
 * for common validation purposes. These regular expressions can be used to validate
 * different types of data inputs.
 */
public final class Regex {

    private Regex() {
        throw new IllegalInstantiationException();
    }

    public static final String SPACE = "\\s+";

    public static final String COUNTRY_CODE = "[A-Z]{2}";

    public static final String EMAIL = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
    public static final String PHONE = "^\\\\+[1-9]\\\\d{1,14}$";
    public static final String POSTAL_CODE = "^\\d{5,6}$";
    public static final String UUID = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$";

}