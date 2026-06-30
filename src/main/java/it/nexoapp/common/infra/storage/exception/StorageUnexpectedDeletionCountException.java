package it.nexoapp.common.infra.storage.exception;


import jakarta.ws.rs.core.Response;

import java.io.Serial;

public class StorageUnexpectedDeletionCountException extends StorageRuntimeException {

    @Serial
    private static final long serialVersionUID = -8747794000787382721L;

    public static final Response.Status RESPONSE_CODE = Response.Status.INTERNAL_SERVER_ERROR;
    public static final String CODE =  DEFAULT_CODE + ".storage.unexpected.row.deletion";
    public static final String MESSAGE = "Delete query returned unexpected number of rows removed. Please check your query.";

    public StorageUnexpectedDeletionCountException() {
        super(RESPONSE_CODE, CODE, MESSAGE);
    }

}