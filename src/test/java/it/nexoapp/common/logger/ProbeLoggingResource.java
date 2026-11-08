package it.nexoapp.common.logger;

import it.nexoapp.common.security.AllowUnlinked;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Test-only resource that echoes a JSON request, so the tests can check that neither the request nor the response
 * body reaches the logs. Exempt from the identity gate so the tests need no membership.
 */
@Path("/probe-logging")
@AllowUnlinked
public class ProbeLoggingResource {

    /**
     * Typed client of the text endpoint, used to produce an outbound call.
     */
    @Path("/probe-logging")
    public interface ProbeClient {

        /**
         * Calls the text endpoint.
         *
         * @param query  query string value the logs must never show
         * @param bearer credential header value the logs must never show
         * @param callId call id header
         * @return the raw response
         */
        @GET
        @Path("/text")
        Response text(@QueryParam("q") String query, @HeaderParam("Authorization") String bearer,
                      @HeaderParam("X-Call-Id") String callId);

    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Nested element of the probe request.
     *
     * @param a      plain value
     * @param secret value the logs must never show
     */
    public record Item(String a, @Sensitive String secret) {
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Probe request.
     *
     * @param name    plain value
     * @param taxCode personal data the logs must never show
     * @param items   nested records carrying a secret each
     */
    public record ProbeLoggingRequest(String name, @Sensitive String taxCode, List<Item> items) {
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

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Accepts a multipart upload and answers with the number of bytes received.
     *
     * @param field plain form field, value the logs must never show
     * @param file  uploaded file, content the logs must never show
     * @return a plain text answer
     */
    @POST
    @Path("/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String multipart(@RestForm final String field, @RestForm final FileUpload file) {
        return "received " + (file == null ? 0 : file.size()) + " bytes, field " + field;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Answers with a plain text body.
     *
     * @return text the logs must never show
     */
    @GET
    @Path("/text")
    @Produces(MediaType.TEXT_PLAIN)
    public String text() {
        return "plain-text-secret";
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Answers with a binary stream.
     *
     * @return bytes the logs must never show
     */
    @GET
    @Path("/stream")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream stream() {
        return new ByteArrayInputStream("stream-secret".getBytes(StandardCharsets.UTF_8));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Makes an outbound call to the text endpoint of this resource, with a query string and a credential header
     * the logs must never show.
     *
     * @param uriInfo the request URI information, used to find this server
     * @return the status of the outbound call
     */
    @GET
    @Path("/call")
    @Produces(MediaType.TEXT_PLAIN)
    public String call(@Context final UriInfo uriInfo) {
        final ProbeClient client = RestClientBuilder.newBuilder().baseUri(uriInfo.getBaseUri()).build(ProbeClient.class);
        try (Response response = client.text("query-secret", "Bearer client-secret", "client-call-1")) {
            return "status " + response.getStatus();
        }
    }

}
