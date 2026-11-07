package it.nexoapp.common.infra.storage.exception;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Abstract base class for runtime exceptions related to storage operations.
 * Extends {@link AbstractRuntimeException} to standardize error handling for storage-related issues.
 * Provides multiple constructors for flexibility in specifying error details.
 */
public abstract class StorageRuntimeException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 6523165205514472864L;

    /**
     * {@inheritDoc}
     * @param status {@inheritDoc}
     * @param code {@inheritDoc}
     * @param message {@inheritDoc}
     * @param cause {@inheritDoc}
     */
    protected StorageRuntimeException(
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
    protected StorageRuntimeException(
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
    protected StorageRuntimeException(
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
    protected StorageRuntimeException(final String code, final String message) {
        super(code, message);
    }

    /**
     * {@inheritDoc}
     * @param message {@inheritDoc}
     * @param cause {@inheritDoc}
     */
    protected StorageRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     * @param message {@inheritDoc}
     */
    protected StorageRuntimeException(final String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     * @param cause {@inheritDoc}
     */
    protected StorageRuntimeException(final Throwable cause) {
        super(cause);
    }

}
