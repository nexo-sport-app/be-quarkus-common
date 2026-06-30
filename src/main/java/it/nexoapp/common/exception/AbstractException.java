package it.nexoapp.common.exception;

import it.nexoapp.common.domain.DateTime;
import jakarta.ws.rs.core.Response;
import lombok.Getter;

import java.io.Serial;


@Getter
public abstract class AbstractException extends Exception {

    public static final String DEAFULT_CODE_PREFIX = "nexo.exception";
    public static final String DEFAULT_CODE = DEAFULT_CODE_PREFIX + ".unexpected";

    @Serial
    private static final long serialVersionUID = -4812524470550917602L;

    private final Response.Status status;
    private final String code;
    private final String message;
    private final DateTime timestamp;

    protected AbstractException(final Response.Status status, final String code, final String message, final Throwable cause) {
        super(cause);
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = DateTime.now();
    }

    protected AbstractException(final Response.Status status, final String code, final String message) {
        super(message);
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = DateTime.now();
    }

    protected AbstractException(final String code, final String message, final Throwable cause) {
        this(Response.Status.INTERNAL_SERVER_ERROR, code, message, cause);
    }

    protected AbstractException(final String code, final String message) {
        this(Response.Status.INTERNAL_SERVER_ERROR, code, message);
    }

    protected AbstractException(final String message, final Throwable cause) {
        this(DEFAULT_CODE, message, cause);
    }

    protected AbstractException(final String message) {
        this(DEFAULT_CODE, message);
    }

    protected AbstractException(final Throwable cause) {
        this(DEFAULT_CODE, cause.getMessage(), cause);
    }
    
}