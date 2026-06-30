package it.nexoapp.common.infra.storage.exception;

import it.nexoapp.common.exception.AbstractException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Represents exceptions that occur during storage operations.
 * This class serves as the base for handling checked exceptions related to storage.
 * Extends {@link AbstractException} to facilitate error details like status, code, and messages.
 */
public abstract class StorageException extends AbstractException {

    @Serial
    private static final long serialVersionUID = 1659571690174765417L;

    /**
     * {@inheritDoc}
     * @param status {@inheritDoc}
     * @param code {@inheritDoc}
     * @param message {@inheritDoc}
     * @param cause {@inheritDoc}
     */
    protected StorageException(
            final Response.Status status,
            final String code,
            final String message,
            final Throwable cause
    ) {
        super(status, code, message, cause);
    }

    /**
     * {@inheritDoc}
     * @param status {@inheritDoc}
     * @param code {@inheritDoc}
     * @param message {@inheritDoc}
     */
    protected StorageException(
            final Response.Status status,
            final String code,
            final String message
    ) {
        super(status, code, message);
    }

    /**
     * {@inheritDoc}
     * @param code {@inheritDoc}
     * @param message {@inheritDoc}
     * @param cause {@inheritDoc}
     */
    protected StorageException(
            final String code,
            final String message,
            final Throwable cause
    ) {
        super(code, message, cause);
    }

    /**
     * {@inheritDoc}
     * @param code {@inheritDoc}
     * @param message {@inheritDoc}
     */
    protected StorageException(final String code, final String message) {
        super(code, message);
    }

    /**
     * {@inheritDoc}
     * @param message {@inheritDoc}
     * @param cause {@inheritDoc}
     */
    protected StorageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     * @param message {@inheritDoc}
     */
    protected StorageException(final String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     * @param cause {@inheritDoc}
     */
    protected StorageException(final Throwable cause) {
        super(cause);
    }

}