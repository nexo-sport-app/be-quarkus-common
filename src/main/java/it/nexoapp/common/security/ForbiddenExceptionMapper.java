package it.nexoapp.common.security;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import it.nexoapp.common.configuration.AppInfo;
import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.LoggerName;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.domain.DateTime;
import it.nexoapp.common.exception.dto.Fault;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Gives role denials ({@code @RolesAllowed} and the other security annotations) a Fault body (ROL-02, D-04).
 * <p>
 * A caller that is not linked, or has not selected an association, holds no roles, so every role check fails for
 * it. Those callers get exactly the Fault of {@link IdentityNotLinkedException} or
 * {@link AssociationSelectionRequiredException}, so the denial of a role-protected endpoint cannot be told apart
 * from the gate's answer (no oracle). A linked caller lacking the role gets {@code 403 FORBIDDEN}.
 */
@Provider
@RequiredArgsConstructor
class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    /** Fault code of a role denial of a linked caller. */
    static final String CODE = "FORBIDDEN";

    /** Generic message of a role denial of a linked caller. */
    static final String MESSAGE = "The caller is not allowed to perform this operation";

    /* - logger - */
    /** Logger; it never receives identifiers of the caller. */
    private static final Logger logger = LoggerFactory.getLogger(LoggerName.EXCEPTION);

    /* - security - */
    /** Identity of the current request. */
    private final SecurityIdentity identity;

    /* - application - */
    /** Application info, the origin of the Fault when the request context was not populated yet. */
    private final AppInfo appInfo;

    /* - jax-rs - */
    /** Headers of the current request, the source of the call id when the request context was not populated yet. */
    @Context
    HttpHeaders headers;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final ForbiddenException exception) {
        final NexoPrincipal principal = identity.getAttribute(NexoPrincipal.ATTRIBUTE);
        final String code;
        final String message;
        if (principal == null || principal.state() == NexoPrincipal.LinkState.NOT_LINKED) {
            code = IdentityNotLinkedException.CODE;
            message = IdentityNotLinkedException.MESSAGE;
        } else if (principal.state() == NexoPrincipal.LinkState.AMBIGUOUS) {
            code = AssociationSelectionRequiredException.CODE;
            message = AssociationSelectionRequiredException.MESSAGE;
        } else {
            code = CODE;
            message = MESSAGE;
        }
        logger.debug("Access denied with code {}", code);
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(
                        Fault.builder()
                                .trace(trace())
                                .origin(StringUtils.defaultIfEmpty(MDCUtils.get(MDCConstant.APP_CODE), appInfo.code()))
                                .timestamp(DateTime.now())
                                .code(code)
                                .message(message)
                                .build()
                )
                .build();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Resolves the trace of the Fault. A role denial can be raised by the eager security check before the request
     * context filter ran, so the MDC may be empty; the call id header and, last, a fresh id stand in.
     *
     * @return the call id of the request
     */
    private String trace() {
        final String fromMdc = MDCUtils.get(MDCConstant.CALL_ID);
        if (StringUtils.isNotEmpty(fromMdc)) {
            return fromMdc;
        }
        final String fromHeader = headers == null ? null : headers.getHeaderString(Headers.CALL_ID);
        return StringUtils.defaultIfEmpty(fromHeader, UUID.randomUUID().toString());
    }

}
