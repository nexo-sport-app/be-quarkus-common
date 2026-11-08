package it.nexoapp.common.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

/**
 * Fail-closed gate of every endpoint (ROL-02, D-01, D-04): an authenticated identity that is not linked to a
 * membership gets {@code 403 IDENTITY_NOT_LINKED}, one with an unresolved selection among several memberships
 * gets {@code 403 ASSOCIATION_SELECTION_REQUIRED}, whether or not the endpoint carries role annotations. Only
 * resources annotated with {@link AllowUnlinked} are exempt. Anonymous identities are left to the HTTP
 * permission layer.
 * <p>
 * The decision reads only the {@link NexoPrincipal} the augmentor attached; a missing attribute on an
 * authenticated identity counts as not linked.
 */
@Provider
@Priority(Priorities.AUTHORIZATION + 10)
@RequiredArgsConstructor
class IdentityGateFilter implements ContainerRequestFilter {

    /* - security - */
    /** Identity of the current request. */
    private final SecurityIdentity identity;

    /* - jax-rs - */
    /** The matched resource class and method, resolved per request. */
    @Context
    ResourceInfo resourceInfo;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @throws IdentityNotLinkedException              when the identity has no membership
     * @throws AssociationSelectionRequiredException   when the identity has several memberships and no valid selection
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) {
        if (identity.isAnonymous() || isExempt()) {
            return;
        }
        final NexoPrincipal principal = identity.getAttribute(NexoPrincipal.ATTRIBUTE);
        if (principal == null) {
            throw new IdentityNotLinkedException();
        }
        switch (principal.state()) {
            case LINKED -> {
                // the caller may proceed
            }
            case AMBIGUOUS -> throw new AssociationSelectionRequiredException();
            case NOT_LINKED -> throw new IdentityNotLinkedException();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tells whether the matched resource method or its class is annotated with {@link AllowUnlinked}.
     *
     * @return true when unlinked callers may reach the endpoint
     */
    private boolean isExempt() {
        if (resourceInfo == null) {
            return false;
        }
        final Method method = resourceInfo.getResourceMethod();
        if (method != null && method.isAnnotationPresent(AllowUnlinked.class)) {
            return true;
        }
        final Class<?> type = resourceInfo.getResourceClass();
        return type != null && type.isAnnotationPresent(AllowUnlinked.class);
    }

}
