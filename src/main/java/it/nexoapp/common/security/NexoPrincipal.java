package it.nexoapp.common.security;

import java.util.Set;
import java.util.UUID;

/**
 * The resolved principal of a request: whether the identity is linked to a membership, and when it is, the
 * association, the member and the roles. It never carries personal data and is attached to the
 * {@code SecurityIdentity} under {@link #ATTRIBUTE}.
 *
 * @param state         how the identity resolved against the local directory, never null
 * @param key           the issuer and subject of the caller, never null
 * @param associationId the active association; non-null exactly when {@code state} is {@code LINKED}
 * @param memberId      the member inside the association; non-null exactly when {@code state} is {@code LINKED}
 * @param roles         roles held in the active association; immutable copy, empty unless {@code LINKED}
 */
public record NexoPrincipal(LinkState state, IdentityKey key, UUID associationId, UUID memberId, Set<Role> roles) {

    /** Name of the {@code SecurityIdentity} attribute holding the principal. */
    public static final String ATTRIBUTE = "nexo.principal";

    /**
     * Resolution outcome of an identity against the local directory.
     */
    public enum LinkState {

        /** The identity maps to exactly one active membership. */
        LINKED,

        /** The identity maps to no membership. */
        NOT_LINKED,

        /** The identity maps to several memberships and none was selected. */
        AMBIGUOUS

    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Enforces the invariants of every state and takes an immutable defensive copy of the roles.
     *
     * @throws IllegalArgumentException when state or key is null, when a LINKED principal lacks association or
     *                                  member, or when a NOT_LINKED or AMBIGUOUS one carries association,
     *                                  member or roles
     */
    public NexoPrincipal {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        if (state == LinkState.LINKED) {
            if (associationId == null || memberId == null) {
                throw new IllegalArgumentException("a LINKED principal requires association and member");
            }
        } else if (associationId != null || memberId != null || !roles.isEmpty()) {
            throw new IllegalArgumentException("a " + state + " principal carries no association, member or roles");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates the principal of an identity linked to one membership.
     *
     * @param key        the identity
     * @param membership the membership whose association, member and roles are copied
     * @return a LINKED principal
     */
    public static NexoPrincipal linked(final IdentityKey key, final MembershipRef membership) {
        return new NexoPrincipal(LinkState.LINKED, key, membership.associationId(), membership.memberId(), membership.roles());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates the principal of an identity with no membership.
     *
     * @param key the identity
     * @return a NOT_LINKED principal without association, member or roles
     */
    public static NexoPrincipal notLinked(final IdentityKey key) {
        return new NexoPrincipal(LinkState.NOT_LINKED, key, null, null, Set.of());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates the principal of an identity with several memberships and no selection.
     *
     * @param key the identity
     * @return an AMBIGUOUS principal without association, member or roles
     */
    public static NexoPrincipal ambiguous(final IdentityKey key) {
        return new NexoPrincipal(LinkState.AMBIGUOUS, key, null, null, Set.of());
    }

}
