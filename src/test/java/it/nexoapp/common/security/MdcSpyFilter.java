package it.nexoapp.common.security;

import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Test-only response filter that records the call id the MDC still holds when the response logging filter runs,
 * proving the per-request MDC cleanup does not happen before the other response filters.
 */
@Provider
@Priority(Interceptor.Priority.APPLICATION + 100)
public class MdcSpyFilter implements ContainerResponseFilter {

    /* - state - */
    /** Call id seen in the MDC by the last response, null when the MDC was already empty. */
    static final AtomicReference<String> LAST_CALL_ID = new AtomicReference<>();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        LAST_CALL_ID.set(MDCUtils.get(MDCConstant.CALL_ID));
    }

}
