package it.nexoapp.common.security;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Raised when an authenticated identity is not linked to any membership (D-04). The message is generic on
 * purpose: it never carries identifiers, and it is identical whatever the reason for the missing link, so it
 * cannot be used to probe which email addresses are known.
 */
public class IdentityNotLinkedException extends AbstractRuntimeException {

    /** Fault code the frontend uses to route the user to onboarding. */
    public static final String CODE = "IDENTITY_NOT_LINKED";

    /** Generic message, identical for every unlinked caller. */
    public static final String MESSAGE = "The identity is not linked to an association";

    @Serial
    private static final long serialVersionUID = 4620153377810274551L;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates the exception with the fixed code and generic message, mapped to 403.
     */
    public IdentityNotLinkedException() {
        super(Response.Status.FORBIDDEN, CODE, MESSAGE);
    }

}
