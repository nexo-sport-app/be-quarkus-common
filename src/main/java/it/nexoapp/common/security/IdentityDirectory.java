package it.nexoapp.common.security;

import java.util.List;

/**
 * Local lookup of the memberships of an identity (D-06). Each service implements it over its own data: users
 * over {@code identity_link}, the other services over their {@code access_grant} projection. It is called from
 * a blocking context and never calls another service.
 */
public interface IdentityDirectory {

    /**
     * Returns the memberships of the identity.
     *
     * @param key the issuer and subject of the caller
     * @return the memberships, empty for an unknown identity, never null
     */
    List<MembershipRef> memberships(IdentityKey key);

}
