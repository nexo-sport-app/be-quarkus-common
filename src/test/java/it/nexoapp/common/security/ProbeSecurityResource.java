package it.nexoapp.common.security;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
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
