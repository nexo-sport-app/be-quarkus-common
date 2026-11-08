package it.nexoapp.common.exception.mapper;

import it.nexoapp.common.configuration.AppInfo;
import it.nexoapp.common.constant.LoggerName;
import it.nexoapp.common.exception.dto.Fault;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps the HTTP status of routing and protocol errors (404, 405, 415, path-parameter conversion failures) and
 * gives them a {@link Fault} body (FND-05), instead of letting the blanket runtime mapper turn them into 500.
 * <p>
 * The headers of the original response (for example {@code Allow} of a 405) are kept. A response that already
 * carries an entity was shaped on purpose and is returned untouched.
 */
@Provider
@RequiredArgsConstructor
class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    /** Prefix of the fault code; the status follows. */
    static final String CODE_PREFIX = "nexo.http.";

    /* - logger - */
    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(LoggerName.EXCEPTION);

    /* - application - */
    /** Application info, the origin of the Fault when the MDC holds none. */
    private final AppInfo appInfo;

    /* - jax-rs - */
    /** Headers of the current request, the source of the call id when the MDC holds none. */
    @Context
    HttpHeaders headers;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final WebApplicationException exception) {
        final Response original = exception.getResponse();
        if (original.hasEntity()) {
            return original;
        }
        final int status = original.getStatus();
        final String code = CODE_PREFIX + status;
        final String message = original.getStatusInfo().getReasonPhrase();
        final Fault fault = FaultFactory.fault(appInfo, headers, code, message);
        if (status >= 500) {
            logger.error("Server error {} code={} callId={}", status, code, fault.getTrace(), exception);
        } else if (status >= 400) {
            logger.warn("Client error {} code={} message={} callId={}", status, code, message, fault.getTrace());
        }
        return Response.fromResponse(original).type(MediaType.APPLICATION_JSON).entity(fault).build();
    }

}
