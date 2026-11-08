package it.nexoapp.common.constant;

/**
 * MDCConstant is an enumeration that defines the keys used for storing
 * diagnostic context information in logging systems.
 * Each constant represents a specific type of contextual data that can
 * be associated with log messages to provide additional context for
 * troubleshooting and system monitoring.
 * <p>
 * Only identifiers and technical data are allowed: personal data (names, usernames, emails, session ids)
 * never goes into the MDC, because it would be copied into every log line.
 */
public enum MDCConstant {

    /* REQUEST related */
    CALL_ID,
    CALLER,
    APP_VERSION,
    IP,

    /* CALLER related (UUIDs only) */
    ASSOCIATION_ID,
    MEMBER_ID,

    /* MICROSERVICE / APP related */
    APP_CODE,
    APP_NODE
    ;

}
