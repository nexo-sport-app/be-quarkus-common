package it.nexoapp.common.exception;

import jakarta.ws.rs.core.Response;

/**
 * Represents an unexpected exception that occurs within the application.
 */
public class UnexpectedException extends AbstractRuntimeException {

    public UnexpectedException(final Throwable cause) {
        super(Response.Status.INTERNAL_SERVER_ERROR, AbstractException.DEFAULT_CODE, cause.getMessage(), cause);
    }

}