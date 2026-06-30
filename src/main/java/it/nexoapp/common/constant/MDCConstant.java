package it.nexoapp.common.constant;

/**
 * MDCConstant is an enumeration that defines a set of keys used for storing
 * diagnostic context information in logging systems.
 * Each constant represents a specific type of contextual data that can
 * be associated with log messages to provide additional context for
 * troubleshooting and system monitoring.
 */
public enum MDCConstant {

    /* USER / SESSION related */
    SESSION_ID,
    USER_ID,
    USERNAME,
    EMAIL,
    CALL_ID,
    CALLER,
    APP_VERSION,
    IP,

    /* MICROSERVICE / APP related */
    APP_CODE,
    APP_NODE
    ;

}