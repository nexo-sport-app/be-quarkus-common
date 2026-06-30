package it.nexoapp.common.infra.transcoding.exception;

import it.nexoapp.common.exception.AbstractException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Parent Exception to all Transaction exception
 */
public abstract class TranscodingException extends AbstractException {

    @Serial
    private static final long serialVersionUID = -1585965744363792005L;

    /* - constant - */
    protected static final String DEFAULT_CODE = AbstractException.DEFAULT_CODE + ".transcoding";

    /**
     * Full constructor for TranscodingException
     * @param status  the HTTP response status
     * @param code    the unique error code
     * @param message the error message
     * @param cause   the underlying cause of the exception
     */
    protected TranscodingException(
            final Response.Status status,
            final String code,
            final String message,
            final Throwable cause
    ) {
        super(status, code, message, cause);
    }

    /**
     * Full constructor for TranscodingException
     * @param status  the HTTP response status
     * @param code    the unique error code
     * @param message the error message
     */
    protected TranscodingException(
            final Response.Status status,
            final String code,
            final String message
    ) {
        super(status, code, message);
    }

}