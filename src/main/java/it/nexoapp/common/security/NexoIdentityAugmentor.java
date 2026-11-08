package it.nexoapp.common.security;

import io.smallrye.mutiny.Uni;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Turns an authenticated OIDC identity into a {@link NexoPrincipal}.
 * <p>
 * The caller is identified by the token issuer and subject only (ROL-03, D-01); memberships and roles come from
 * the service-local {@link IdentityDirectory} (D-06), never from token claims. The augmentor only attaches
 * state: it never throws a domain exception, because an exception raised here would surface as a 401. The 403
 * decisions belong to the JAX-RS layer.
 */
@ApplicationScoped
@RequiredArgsConstructor
class NexoIdentityAugmentor implements SecurityIdentityAugmentor {

    /* - logger - */
    /** Logger; it never receives emails, names or subject values. */
    private static final Logger logger = LoggerFactory.getLogger(NexoIdentityAugmentor.class);

    /* - constants - */
    /** Placeholder key of an identity that carries no usable issuer and subject. */
    private static final IdentityKey UNIDENTIFIED = new IdentityKey("urn:nexo:unidentified", "unidentified");

    /* - directory - */
    /** Service-local directory; absent in a service that did not provide one. */
    private final Instance<IdentityDirectory> directory;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Uni<SecurityIdentity> augment(final SecurityIdentity identity, final AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        final IdentityKey key = keyOf(identity);
        if (key == null) {
            logger.debug("Identity without issuer and subject: principal is NOT_LINKED");
            return Uni.createFrom().item(attach(identity, NexoPrincipal.notLinked(UNIDENTIFIED)));
        }
        if (directory.isUnsatisfied()) {
            logger.debug("No identity directory available: principal is NOT_LINKED");
            return Uni.createFrom().item(attach(identity, NexoPrincipal.notLinked(key)));
        }
        return context.runBlocking(() -> resolve(identity, key));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Resolves the memberships of the identity and attaches the resulting principal.
     *
     * @param identity the authenticated identity
     * @param key      issuer and subject of the caller
     * @return the augmented identity
     */
    private SecurityIdentity resolve(final SecurityIdentity identity, final IdentityKey key) {
        final List<MembershipRef> memberships = directory.get().memberships(key);
        final NexoPrincipal principal;
        if (memberships.size() == 1) {
            principal = NexoPrincipal.linked(key, memberships.getFirst());
        } else if (memberships.isEmpty()) {
            principal = NexoPrincipal.notLinked(key);
        } else {
            principal = NexoPrincipal.ambiguous(key);
        }
        logger.debug("Identity resolved: {}", principal.state());
        return attach(identity, principal);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds the identity key from the issuer and subject of the token.
     *
     * @param identity the authenticated identity
     * @return the key, or null when the principal is not a JWT or lacks issuer or subject
     */
    private static IdentityKey keyOf(final SecurityIdentity identity) {
        if (identity.getPrincipal() instanceof JsonWebToken token && !isBlank(token.getIssuer())
                && !isBlank(token.getSubject())) {
            return new IdentityKey(token.getIssuer(), token.getSubject());
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Attaches the principal to the identity, adding the role names of a LINKED principal.
     *
     * @param identity  the authenticated identity
     * @param principal the resolved principal
     * @return the identity carrying the principal attribute
     */
    private static SecurityIdentity attach(final SecurityIdentity identity, final NexoPrincipal principal) {
        final QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity)
                .addAttribute(NexoPrincipal.ATTRIBUTE, principal);
        principal.roles().forEach(role -> builder.addRole(role.name()));
        return builder.build();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tells whether a claim value is missing.
     *
     * @param value the claim value
     * @return true when null or blank
     */
    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

}
