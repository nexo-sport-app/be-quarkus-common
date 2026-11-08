package it.nexoapp.common.security;

import java.util.Optional;

/**
 * Optional hook, implemented by the users service only, that links a first-time identity to a pre-registered
 * member (the President bootstrap). The augmentor invokes it only while the identity has no membership and
 * only with an IdP-verified email address; it is never invoked after the identity is linked (D-03, amended
 * 2026-11-08). Implementations must be idempotent and safe under concurrency.
 */
public interface IdentityBootstrap {

    /**
     * Tries to link the identity to a pre-registered member.
     *
     * @param key           the issuer and subject of the caller
     * @param verifiedEmail the email address the IdP has verified for the caller
     * @return the membership created or found, empty when nothing matches
     */
    Optional<MembershipRef> link(IdentityKey key, String verifiedEmail);

}
