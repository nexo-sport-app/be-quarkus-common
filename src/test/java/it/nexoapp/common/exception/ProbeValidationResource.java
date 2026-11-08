package it.nexoapp.common.exception;

import it.nexoapp.common.security.AllowUnlinked;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Test-only resource that exercises request validation and routing errors. Exempt from the identity gate so the
 * tests need no membership.
 */
@Path("/probe-validation")
@AllowUnlinked
public class ProbeValidationResource {

    /**
     * Constants of the enum path-parameter probe.
     */
    public enum ProbeKind {
        /** First constant. */
        FIRST,
        /** Second constant. */
        SECOND
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Accepts a valid request and echoes nothing.
     *
     * @param request the validated request
     * @return the name of the request
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String create(@Valid @NotNull final ProbeValidationRequest request) {
        return request.name();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * GET-only endpoint, used to provoke a 405 with another HTTP method.
     *
     * @return a constant text
     */
    @GET
    @Path("/readonly")
    @Produces(MediaType.TEXT_PLAIN)
    public String readOnly() {
        return "ok";
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Endpoint with an enum path parameter; an unknown constant fails the conversion.
     *
     * @param kind the converted constant
     * @return the name of the constant
     */
    @GET
    @Path("/kind/{kind}")
    @Produces(MediaType.TEXT_PLAIN)
    public String kind(@PathParam("kind") final ProbeKind kind) {
        return kind.name();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Endpoint that fails with an unexpected error, used to check server-error handling.
     *
     * @return never returns
     */
    @GET
    @Path("/boom")
    @Produces(MediaType.APPLICATION_JSON)
    public String boom() {
        throw new IllegalStateException("probe failure");
    }

}
