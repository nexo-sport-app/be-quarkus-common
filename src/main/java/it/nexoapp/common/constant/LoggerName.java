package it.nexoapp.common.constant;

import it.nexoapp.common.exception.IllegalInstantiationException;

public final class LoggerName {

    private LoggerName() {
        throw new IllegalInstantiationException();
    }

    public static final String EXCEPTION = "EXCEPTION";
    public static final String SQL = "SQL";
    public static final String CLIENT = "CLIENT";
    public static final String SERVER = "SERVER";

}
