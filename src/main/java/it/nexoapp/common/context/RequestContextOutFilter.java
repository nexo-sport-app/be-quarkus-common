package it.nexoapp.common.context;

import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Closes the request: adds the call id to the response and clears the per-request MDC keys, so a worker
 * thread reused by another request never carries the previous caller's association or member (D-20).
 * <p>
 * JAX-RS runs response filters in descending priority order, so the lowest priority runs last: this filter
 * must run after every other response filter (the response logging one included), which still need the MDC.
 */
@Provider
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class RequestContextOutFilter implements ContainerResponseFilter {

    /* - constants - */
    /** MDC keys set per request; the application code and node are constant and stay. */
    private static final MDCConstant[] REQUEST_KEYS = {
            MDCConstant.CALL_ID, MDCConstant.ASSOCIATION_ID, MDCConstant.MEMBER_ID,
            MDCConstant.IP, MDCConstant.CALLER, MDCConstant.APP_VERSION
    };

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        responseContext.getHeaders().add(Headers.CALL_ID, MDCUtils.get(MDCConstant.CALL_ID));
        for (final MDCConstant key : REQUEST_KEYS) {
            MDCUtils.remove(key);
        }
    }

}
