package it.nexoapp.common.constant;

import it.nexoapp.common.exception.IllegalInstantiationException;

/**
 * The Constant class is a utility class that defines a collection of static final variables
 * commonly used as constants throughout the application. These constants are designed to
 * provide standardized placeholder values for various scenarios.
 */
public final class Constant {

    private Constant() {
        throw new IllegalInstantiationException();
    }

    /** Represents a standardized "Not Available" placeholder value. */
    public static final String N_A = "N/A";

    /** Represents an empty string value. */
    public static final String EMPTY = "";

    /** Represents a single space character. */
    public static final String SPACE = " ";

    /** Represents a placeholder value for unavailable user IDs. */
    public static final String NO_USER_ID = "__NO_USER_ID_HERE__";

    /** Represents the minimum date value used in date range validations. */
    public static final String MIN_DATE = "1900-01-01";

    /** Represents the maximum date value used in date range validations. */
    public static final String MAX_DATE = "2999-12-31";

}