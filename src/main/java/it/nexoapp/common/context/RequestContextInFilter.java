package it.nexoapp.common.context;

import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.ext.web.RoutingContext;
import it.nexoapp.common.configuration.AppInfo;
import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.Language;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.security.NexoPrincipal;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Fills the request-scoped {@link RequestContext} and the MDC at the start of every request.
 * <p>
 * Only identifiers are copied: the call id, the client address, the language and, for a caller resolved by
 * the identity augmentor, the subject, the association and the member. No provider-specific claim (session id,
 * username, names, email) is read here, so no personal data reaches the MDC or the logs (D-20, ROL-03).
 * The MDC is cleared by {@link RequestContextOutFilter}.
 */
@Provider
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
@RequiredArgsConstructor
public class RequestContextInFilter implements ContainerRequestFilter {

    /* - configuration - */
    /** Application identity, source of the code and node MDC values. */
    private final AppInfo appInfo;

    /* - request - */
    /** Request-scoped context to populate. */
    private final RequestContext requestContext;

    /** Vert.x routing context of the current request, source of the client address. */
    private final RoutingContext routingContext;

    /* - security - */
    /** Identity of the current request, with the principal attached by the augmentor. */
    private final SecurityIdentity identity;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext rc) {
        // SERVICE INFO
        MDCUtils.put(MDCConstant.APP_CODE, appInfo.code());
        MDCUtils.put(MDCConstant.APP_NODE, appInfo.hostname());

        // REQUEST INFO
        requestContext.setHeaders(rc.getHeaders());

        final String callId = StringUtils.defaultIfEmpty(rc.getHeaders().getFirst(Headers.CALL_ID), UUID.randomUUID().toString());
        MDCUtils.put(MDCConstant.CALL_ID, callId);
        requestContext.setCallId(callId);

        final String clientIpAddress = routingContext.request().remoteAddress().host();
        MDCUtils.put(MDCConstant.IP, clientIpAddress);
        requestContext.setIp(clientIpAddress);

        requestContext.setLanguage(Language.fromLocale(rc.getLanguage()));

        // CALLER INFO (identifiers only)
        final NexoPrincipal principal = identity.getAttribute(NexoPrincipal.ATTRIBUTE);
        if (principal == null) {
            return;
        }
        requestContext.setSubject(principal.key().subject());
        if (principal.state() == NexoPrincipal.LinkState.LINKED) {
            requestContext.setAssociationId(principal.associationId());
            requestContext.setMemberId(principal.memberId());
            MDCUtils.put(MDCConstant.ASSOCIATION_ID, principal.associationId().toString());
            MDCUtils.put(MDCConstant.MEMBER_ID, principal.memberId().toString());
        }
    }

}
