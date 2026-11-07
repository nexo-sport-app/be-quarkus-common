package it.nexoapp.common.infra.storage.exception;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.MediaType;

import java.io.Serial;

/**
 * Exception thrown when the content of a storage is not of the expected type.
 */
public class IllegalRequestedStorageContentException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 348285509426131457L;

    public static final String CODE = DEFAULT_CODE + ".storage.illegal.content";
    public static final String MESSAGE = "Illegal storage content. expected='%s' but requested='%s'";

    public IllegalRequestedStorageContentException(final String contentType, final Class<?> requestedType) {
        super(CODE, MESSAGE.formatted(contentType, requestedType.getSimpleName()));
    }

    public IllegalRequestedStorageContentException(final MediaType contentType, final Class<?> requestedType) {
        this(contentType.toString(), requestedType);
    }

}