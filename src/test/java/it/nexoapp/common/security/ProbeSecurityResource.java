package it.nexoapp.common.security;

import io.quarkus.security.identity.SecurityIdentity;
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

}
