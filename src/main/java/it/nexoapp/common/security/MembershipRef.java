package it.nexoapp.common.security;

import java.util.Set;
import java.util.UUID;

/**
 * Reference to one membership of an identity: the association, the member inside it and the roles held there.
 * It is what an {@link IdentityDirectory} returns; it carries identifiers and roles only, no personal data.
 *
 * @param associationId the association the membership belongs to, never null
 * @param memberId      the member record inside the association, never null
 * @param roles         the roles held in that association; immutable copy, empty when none
 */
public record MembershipRef(UUID associationId, UUID memberId, Set<Role> roles) {

    /**
     * Validates the identifiers and takes an immutable defensive copy of the roles (empty when null).
     *
     * @throws IllegalArgumentException when the association or the member identifier is null
     */
    public MembershipRef {
        if (associationId == null) {
            throw new IllegalArgumentException("associationId must not be null");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("memberId must not be null");
        }
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

}
