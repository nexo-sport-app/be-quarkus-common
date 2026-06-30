package it.nexoapp.common.hibernate.exception;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Exception thrown to indicate that a requested entity could not be found.
 * @see AbstractRuntimeException
 */
public class EntityNotFoundException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 4628006600813881850L;

    /* - constant - */
    public static final String CODE = DEAFULT_CODE_PREFIX + "entity.not.found";
    public static final String MESSAGE = "Entity not found with the given parameters.";

    public EntityNotFoundException() {
        super(Response.Status.NOT_FOUND, CODE, MESSAGE);
    }

    public EntityNotFoundException(final String message) {
        super(Response.Status.NOT_FOUND, CODE, MESSAGE + " " + message);
    }

}