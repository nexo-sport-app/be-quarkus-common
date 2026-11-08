package it.nexoapp.common.logger;

import it.nexoapp.common.security.AllowUnlinked;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Test-only resource that echoes a JSON request, so the tests can check that neither the request nor the response
 * body reaches the logs. Exempt from the identity gate so the tests need no membership.
 */
@Path("/probe-logging")
@AllowUnlinked
public class ProbeLoggingResource {

    /**
     * Nested element of the probe request.
     *
     * @param a      plain value
     * @param secret value the logs must never show
     */
    public record Item(String a, String secret) {
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Probe request.
     *
     * @param name    plain value
     * @param taxCode personal data the logs must never show
     * @param items   nested records carrying a secret each
     */
    public record ProbeLoggingRequest(String name, String taxCode, List<Item> items) {
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Echoes the request.
     *
     * @param request the request
     * @return the same request
     */
    @POST
    @Path("/json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProbeLoggingRequest echo(final ProbeLoggingRequest request) {
        return request;
    }

}
