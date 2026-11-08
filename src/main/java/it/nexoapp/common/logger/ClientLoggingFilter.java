package it.nexoapp.common.logger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Writes one DEBUG line per outbound HTTP call: method, URI without query string, status, duration and the
 * allowlisted headers of the request and of the response.
 * <p>
 * No body is ever read or logged, and the query string is dropped because it may carry personal data (D-19).
 */
@Provider
class ClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    /* - constants - */
    /** Request property holding the start time in nanoseconds. */
    private static final String START_PROPERTY = ClientLoggingFilter.class.getName() + ".start";

    /* - logger - */
    /** The logger of the filter. */
    private static final Logger LOG = LoggerFactory.getLogger(ClientLoggingFilter.class);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ClientRequestContext requestContext) {
        requestContext.setProperty(START_PROPERTY, System.nanoTime());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        final long durationMs = requestContext.getProperty(START_PROPERTY) instanceof final Long start
                ? TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                : -1L;
        LOG.debug("client method={} uri={} status={} durationMs={} requestHeaders={} responseHeaders={}",
                requestContext.getMethod(),
                withoutQuery(requestContext.getUri()),
                responseContext.getStatus(),
                durationMs,
                HeaderAllowlist.filter(requestContext.getStringHeaders()),
                HeaderAllowlist.filter(responseContext.getHeaders()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Rebuilds the URI without query string, fragment and user information.
     *
     * @param uri the request URI
     * @return scheme, host, port and raw path only
     */
    private static String withoutQuery(final URI uri) {
        final StringBuilder text = new StringBuilder();
        if (uri.getScheme() != null) {
            text.append(uri.getScheme()).append("://");
        }
        if (uri.getHost() != null) {
            text.append(uri.getHost());
        }
        if (uri.getPort() >= 0) {
            text.append(':').append(uri.getPort());
        }
        if (uri.getRawPath() != null) {
            text.append(uri.getRawPath());
        }
        return text.toString();
    }

}
