package it.nexoapp.common.infra.storage.exception;

import jakarta.ws.rs.core.Response;

import java.io.Serial;
import java.util.UUID;

/**
 * {@inheritDoc}
 */
public class StorageNotFoundException extends StorageRuntimeException {

    @Serial
    private static final long serialVersionUID = 7713947847830961517L;

    public static final Response.Status RESPONSE_CODE = Response.Status.NOT_FOUND;
    public static final String CODE =  DEFAULT_CODE + ".storage.not.found";
    public static final String MESSAGE = "Storage not found with the given parameters.";

    /**
     * {@inheritDoc}
     * @param id {@inheritDoc}
     */
    public StorageNotFoundException(final UUID id) {
        super(RESPONSE_CODE, CODE, MESSAGE + " id='" + id + "'");
    }

    /**
     * {@inheritDoc}
     * @param type {@inheritDoc}
     * @param key {@inheritDoc}
     */
    public StorageNotFoundException(final String type, final String key) {
        super(RESPONSE_CODE, CODE, MESSAGE + " type='" + type + "', key='" + key + "'");
    }

}