package it.nexoapp.common.security;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Raised when the caller belongs to several associations and did not select one of them with a valid
 * association header (D-01). The message is generic: it never lists the associations of the caller.
 */
public class AssociationSelectionRequiredException extends AbstractRuntimeException {

    /** Fault code the frontend uses to ask the user which association to act in. */
    public static final String CODE = "ASSOCIATION_SELECTION_REQUIRED";

    /** Generic message, identical for a missing and for an invalid selection. */
    public static final String MESSAGE = "An association must be selected for this request";

    @Serial
    private static final long serialVersionUID = -3198824520731466217L;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates the exception with the fixed code and generic message, mapped to 403.
     */
    public AssociationSelectionRequiredException() {
        super(Response.Status.FORBIDDEN, CODE, MESSAGE);
    }

}
