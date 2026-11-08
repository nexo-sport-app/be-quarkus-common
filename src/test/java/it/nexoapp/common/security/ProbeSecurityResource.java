package it.nexoapp.common.security;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.context.RequestContext;
import it.nexoapp.common.tenant.probe.ProbeRepository;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test-only resource that echoes the principal the augmentor attached to the request, so tests can assert on
 * what a real HTTP request sees.
 */
@Path("/probe-security")
public class ProbeSecurityResource {

    /* - security - */
    /** Identity of the current request. */
    @Inject
    SecurityIdentity identity;

    /* - request - */
    /** Request-scoped context populated by the request filter. */
    @Inject
    RequestContext requestContext;

    /* - repository - */
    /** Probe table, tenant-scoped through the request. */
    @Inject
    ProbeRepository probeRepository;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * What the request sees of its principal.
     *
     * @param state         link state of the attached principal, null when none is attached
     * @param associationId association of the principal, null unless LINKED
     * @param memberId      member of the principal, null unless LINKED
     * @param roles         role names of the attached principal
     * @param identityRoles role names of the {@code SecurityIdentity} itself
     */
    public record PrincipalView(String state, String associationId, String memberId, Set<String> roles,
                                Set<String> identityRoles) {
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the principal attached to the current identity.
     *
     * @return the view of the principal
     */
    @GET
    @AllowUnlinked
    @Path("/principal")
    @Produces(MediaType.APPLICATION_JSON)
    public PrincipalView principal() {
        final NexoPrincipal principal = identity.getAttribute(NexoPrincipal.ATTRIBUTE);
        if (principal == null) {
            return new PrincipalView(null, null, null, Set.of(), identity.getRoles());
        }
        return new PrincipalView(
                principal.state().name(),
                principal.associationId() == null ? null : principal.associationId().toString(),
                principal.memberId() == null ? null : principal.memberId().toString(),
                principal.roles().stream().map(Enum::name).collect(Collectors.toSet()),
                identity.getRoles());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * What a gated endpoint returns: the association the request acts in.
     *
     * @param associationId association of the principal, null when the principal is not linked
     */
    public record AssociationView(String associationId) {
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Endpoint restricted to the PRESIDENT role.
     *
     * @return the association of the caller
     */
    @GET
    @RolesAllowed("PRESIDENT")
    @Path("/president")
    @Produces(MediaType.APPLICATION_JSON)
    public AssociationView president() {
        return currentAssociation();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Endpoint open to any authenticated identity; only the identity gate stands in front of it.
     *
     * @return the association of the caller
     */
    @GET
    @Authenticated
    @Path("/authenticated")
    @Produces(MediaType.APPLICATION_JSON)
    public AssociationView authenticated() {
        return currentAssociation();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Endpoint exempt from the identity gate.
     *
     * @return the association of the caller, null when unlinked
     */
    @GET
    @AllowUnlinked
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public AssociationView me() {
        return currentAssociation();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * What the request context and the MDC hold while the request runs.
     *
     * @param callId        MDC value of CALL_ID
     * @param associationId MDC value of ASSOCIATION_ID, null when absent
     * @param memberId      MDC value of MEMBER_ID, null when absent
     * @param subject       subject held by the request context, null when absent
     * @param contextAssociationId association held by the request context, null when absent
     * @param contextMemberId      member held by the request context, null when absent
     */
    public record MdcView(String callId, String associationId, String memberId, String subject,
                          String contextAssociationId, String contextMemberId) {
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Echoes the MDC identifiers and the request context subject seen inside the resource.
     *
     * @return the view of MDC and request context
     */
    @GET
    @AllowUnlinked
    @Path("/mdc")
    @Produces(MediaType.APPLICATION_JSON)
    public MdcView mdc() {
        return new MdcView(
                MDCUtils.get(MDCConstant.CALL_ID),
                MDCUtils.get(MDCConstant.ASSOCIATION_ID),
                MDCUtils.get(MDCConstant.MEMBER_ID),
                requestContext.getSubject(),
                requestContext.getAssociationId() == null ? null : requestContext.getAssociationId().toString(),
                requestContext.getMemberId() == null ? null : requestContext.getMemberId().toString());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Number of probe rows visible to the request.
     *
     * @param count rows under the tenant of the request
     */
    public record CountView(long count) {
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Counts the probe rows visible under the tenant of the request.
     *
     * @return the number of rows of the request's association
     */
    @GET
    @AllowUnlinked
    @Path("/probe-count")
    @Produces(MediaType.APPLICATION_JSON)
    public CountView probeCount() {
        return new CountView(probeRepository.count());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Reads the association of the attached principal.
     *
     * @return the view, with a null association when none is attached or the principal is not linked
     */
    private AssociationView currentAssociation() {
        final NexoPrincipal principal = identity.getAttribute(NexoPrincipal.ATTRIBUTE);
        if (principal == null || principal.associationId() == null) {
            return new AssociationView(null);
        }
        return new AssociationView(principal.associationId().toString());
    }

}
