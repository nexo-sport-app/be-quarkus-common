package it.nexoapp.common.exception;

import it.nexoapp.common.domain.DateTime;
import jakarta.ws.rs.core.Response;
import lombok.Getter;

import java.io.Serial;


/**
 * Base class for all application-specific runtime exceptions in the Nexo application.
 * <p>
 * All custom runtime exceptions should extend this class to ensure consistent error handling,
 * including HTTP status codes, error codes, messages, and timestamps.
 * </p>
 */
@Getter
public abstract class AbstractRuntimeException extends RuntimeException {

    public static final String DEAFULT_CODE_PREFIX = "nexo.runtime.exception";
    public static final String DEFAULT_CODE = DEAFULT_CODE_PREFIX + ".unexpected";

    @Serial
    private static final long serialVersionUID = -4812524470550917602L;

    /**
     * An HTTP status code representation of the exception
     */
    private final Response.Status status;
    /**
     * A unique error code that identifies the type of exception.
     */
    private final String code;
    /**
     * A human-readable message describing the exception.
     */
    private final String message;
    /**
     * The timestamp when the exception was thrown.
     */
    private final DateTime timestamp;

    /**
     * Creates a new runtime exception with full control over all properties.
     * <p>
     * Use this constructor when you need to specify a custom HTTP status, error code, message, and wrap an existing exception.
     * </p>
     *
     * @param status  the HTTP response status
     * @param code    the unique error code
     * @param message the error message
     * @param cause   the underlying cause of the exception
     */
    protected AbstractRuntimeException(final Response.Status status, final String code, final String message, final Throwable cause) {
        super(cause);
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = DateTime.now();
    }

    /**
     * Creates a new runtime exception with a custom HTTP status, error code, and message.
     * <p>
     * Use this constructor when you need to specify all error details but don't have an underlying cause.
     * </p>
     *
     * @param status  the HTTP response status
     * @param code    the unique error code
     * @param message the error message
     */
    protected AbstractRuntimeException(final Response.Status status, final String code, final String message) {
        super(message);
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = DateTime.now();
    }

    /**
     * Creates a new runtime exception with a custom error code and message, wrapping an existing exception.
     * <p>
     * The HTTP status defaults to INTERNAL_SERVER_ERROR (500). Use this when you want to specify
     * a custom error code while wrapping another exception.
     * </p>
     *
     * @param code    the unique error code
     * @param message the error message
     * @param cause   the underlying cause of the exception
     */
    protected AbstractRuntimeException(final String code, final String message, final Throwable cause) {
        this(Response.Status.INTERNAL_SERVER_ERROR, code, message, cause);
    }

    /**
     * Creates a new runtime exception with a custom error code and message.
     * <p>
     * The HTTP status defaults to INTERNAL_SERVER_ERROR (500). Use this when you want to specify
     * a custom error code and message without an underlying cause.
     * </p>
     *
     * @param code    the unique error code
     * @param message the error message
     */
    protected AbstractRuntimeException(final String code, final String message) {
        this(Response.Status.INTERNAL_SERVER_ERROR, code, message);
    }

    /**
     * Creates a new runtime exception with a message and underlying cause.
     * <p>
     * The error code defaults to {@link #DEFAULT_CODE} and the HTTP status defaults to INTERNAL_SERVER_ERROR (500).
     * Use this for simple exception wrapping with a custom message.
     * </p>
     *
     * @param message the error message
     * @param cause   the underlying cause of the exception
     */
    protected AbstractRuntimeException(final String message, final Throwable cause) {
        this(DEFAULT_CODE, message, cause);
    }

    /**
     * Creates a new runtime exception with only a message.
     * <p>
     * The error code defaults to {@link #DEFAULT_CODE} and the HTTP status defaults to INTERNAL_SERVER_ERROR (500).
     * Use this for simple exceptions with just a descriptive message.
     * </p>
     *
     * @param message the error message
     */
    protected AbstractRuntimeException(final String message) {
        this(DEFAULT_CODE, message);
    }

    /**
     * Creates a new runtime exception wrapping an existing throwable.
     * <p>
     * The message is extracted from the cause, and both error code and HTTP status use default values
     * ({@link #DEFAULT_CODE} and INTERNAL_SERVER_ERROR). Use this for simple exception wrapping.
     * </p>
     *
     * @param cause the underlying cause of the exception
     */
    protected AbstractRuntimeException(final Throwable cause) {
        this(DEFAULT_CODE, cause.getMessage(), cause);
    }

}