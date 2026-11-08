package it.nexoapp.common.constant;

import it.nexoapp.common.exception.IllegalInstantiationException;

/**
 * Names of the named loggers of the platform, configured in the common defaults.
 */
public final class LoggerName {

    private LoggerName() {
        throw new IllegalInstantiationException();
    }

    /** Logger of the exception mappers: one line per handled failure. */
    public static final String EXCEPTION = "EXCEPTION";

    /** Logger of the access lines: one INFO line per HTTP request, without headers or bodies. */
    public static final String ACCESS = "ACCESS";

}
