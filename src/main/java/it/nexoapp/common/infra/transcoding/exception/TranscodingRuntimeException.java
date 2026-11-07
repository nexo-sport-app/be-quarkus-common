package it.nexoapp.common.infra.transcoding.exception;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Parent RuntimeExcpetion to all Transaction exception
 */
public abstract class TranscodingRuntimeException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 5096426449574049591L;

    /* - constant - */
    protected static final String DEFAULT_CODE = AbstractRuntimeException.DEFAULT_CODE + ".transcoding";

    /**
     * Full constructor for TranscodingRuntimeException
     * @param status  the HTTP response status
     * @param code    the unique error code
     * @param message the error message
     * @param cause   the underlying cause of the exception
     */
    protected TranscodingRuntimeException(
            final Response.Status status,
            final String code,
            final String message,
            final Throwable cause
    ) {
        super(status, code, message, cause);
    }

}