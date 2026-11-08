package it.nexoapp.common.tenant;

import it.nexoapp.common.exception.AbstractRuntimeException;
import jakarta.ws.rs.core.Response;

import java.io.Serial;

/**
 * Thrown when a Hibernate session opens and no tenant can be resolved. The platform never falls back
 * to a default association: the caller gets a 403 with a generic message that carries no identifiers.
 */
public class TenantNotResolvedException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 6251396170584972811L;

    /** Machine-readable error code. */
    public static final String CODE = "TENANT_NOT_RESOLVED";

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates the exception with the generic message.
     */
    public TenantNotResolvedException() {
        super(Response.Status.FORBIDDEN, CODE, "The association of the request could not be resolved");
    }

}
