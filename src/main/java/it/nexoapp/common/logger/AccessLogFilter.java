package it.nexoapp.common.logger;

import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.LoggerName;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Writes one INFO line per HTTP request on the {@code ACCESS} logger: method, path, status, duration and call id.
 * <p>
 * The line carries no header, no query string, no body and no user data (D-17, D-19): neither the request nor the
 * response entity stream is ever read here. The call id is written into the message because the MDC is cleared by
 * the request context out filter once every other response filter has run.
 * <p>
 * JAX-RS runs response filters in descending priority order: this filter's priority is above the out filter's, so
 * the MDC still holds the call id when the line is written.
 */
@Provider
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 1)
class AccessLogFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /* - constants - */
    /** Request property holding the start time in nanoseconds. */
    private static final String START_PROPERTY = AccessLogFilter.class.getName() + ".start";

    /** Placeholder written when a request has no call id. */
    private static final String NO_CALL_ID = "-";

    /* - logger - */
    /** The access logger. */
    private static final Logger ACCESS = LoggerFactory.getLogger(LoggerName.ACCESS);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {
        requestContext.setProperty(START_PROPERTY, System.nanoTime());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        if (!ACCESS.isInfoEnabled()) {
            return;
        }
        final long durationMs = requestContext.getProperty(START_PROPERTY) instanceof final Long start
                ? TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                : -1L;
        ACCESS.info("method={} path={} status={} durationMs={} callId={}",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().getRawPath(),
                responseContext.getStatus(),
                durationMs,
                callId(requestContext));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Resolves the call id of the request: the MDC value set by the request context filter, else the request
     * header. The value is client-controlled, so everything but printable ASCII is replaced.
     *
     * @param requestContext the request
     * @return the sanitised call id, never null
     */
    private static String callId(final ContainerRequestContext requestContext) {
        String id = MDCUtils.get(MDCConstant.CALL_ID);
        if (id == null || id.isEmpty()) {
            id = requestContext.getHeaderString(Headers.CALL_ID);
        }
        if (id == null || id.isEmpty()) {
            return NO_CALL_ID;
        }
        return id.replaceAll("[^\\x21-\\x7E]", "_");
    }

}
