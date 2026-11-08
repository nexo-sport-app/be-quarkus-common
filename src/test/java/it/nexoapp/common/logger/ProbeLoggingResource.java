package it.nexoapp.common.logger;

import it.nexoapp.common.security.AllowUnlinked;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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

}
