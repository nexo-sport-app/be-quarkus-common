package it.nexoapp.common.hibernate.exception;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;


/**
 * Exception thrown to indicate that an entity with the specified parameters already exists.
 * @see AbstractRuntimeException
 */
public class EntityAlreadyExistException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = -2923213604166621589L;

    /* - constant - */
    public static final String CODE = DEAFULT_CODE_PREFIX + "entity.already.exist";
    public static final String MESSAGE = "Entity already exist with the given parameters";

    public EntityAlreadyExistException() {
        super(Response.Status.CONFLICT, CODE, MESSAGE);
    }

}