package it.nexoapp.common.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import it.nexoapp.common.constant.Headers;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    /* - bootstrap - */
    /** Optional first-login hook, provided by the users service only. */
    private final Instance<IdentityBootstrap> bootstrap;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Uni<SecurityIdentity> augment(final SecurityIdentity identity, final AuthenticationRequestContext context) {
        return augment(identity, context, HttpSecurityUtils.getRoutingContextAttribute(identity));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Uni<SecurityIdentity> augment(final SecurityIdentity identity, final AuthenticationRequestContext context,
                                         final Map<String, Object> attributes) {
        return augment(identity, context, HttpSecurityUtils.getRoutingContextAttribute(attributes));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Resolves the identity against the directory, selecting the association from the request header when the
     * request is available.
     *
     * @param identity the authenticated identity
     * @param context  runs the blocking directory lookup
     * @param routing  the HTTP request being authenticated, null outside an HTTP request
     * @return the augmented identity
     */
    private Uni<SecurityIdentity> augment(final SecurityIdentity identity, final AuthenticationRequestContext context,
                                          final RoutingContext routing) {
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
        final String requested = routing == null ? null : routing.request().getHeader(Headers.ASSOCIATION_ID);
        return context.runBlocking(() -> resolve(identity, key, requested));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Resolves the memberships of the identity and attaches the resulting principal.
     *
     * @param identity  the authenticated identity
     * @param key       issuer and subject of the caller
     * @param requested value of the association header, null or blank when absent
     * @return the augmented identity
     */
    private SecurityIdentity resolve(final SecurityIdentity identity, final IdentityKey key, final String requested) {
        List<MembershipRef> memberships = directory.get().memberships(key);
        if (memberships.isEmpty() && bootstrap.isResolvable()) {
            memberships = bootstrap(identity, key).map(List::of).orElse(List.of());
        }
        final NexoPrincipal principal = select(key, memberships, requested);
        logger.debug("Identity resolved: {}", principal.state());
        return attach(identity, principal);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Picks the membership the request acts on. The header never grants anything on its own: when present it
     * must name one of the verified memberships, otherwise the principal is AMBIGUOUS, also for a caller with a
     * single membership.
     *
     * @param key         issuer and subject of the caller
     * @param memberships verified memberships of the caller
     * @param requested   value of the association header, null or blank when absent
     * @return the resulting principal
     */
    private static NexoPrincipal select(final IdentityKey key, final List<MembershipRef> memberships,
                                        final String requested) {
        if (memberships.isEmpty()) {
            return NexoPrincipal.notLinked(key);
        }
        if (requested == null || requested.isBlank()) {
            return memberships.size() == 1 ? NexoPrincipal.linked(key, memberships.getFirst())
                    : NexoPrincipal.ambiguous(key);
        }
        final Optional<UUID> association = parse(requested.strip());
        return association
                .flatMap(id -> memberships.stream().filter(membership -> membership.associationId().equals(id)).findFirst())
                .map(membership -> NexoPrincipal.linked(key, membership))
                .orElseGet(() -> NexoPrincipal.ambiguous(key));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tries to link a still-unlinked identity to a pre-registered member through the verified email (D-03,
     * amended 2026-11-08). The claims are read here only, only for an identity with no membership, and the email
     * only when the IdP marks it verified. A failing hook leaves the identity unlinked.
     *
     * @param identity the authenticated identity, whose principal is a JWT
     * @param key      issuer and subject of the caller
     * @return the membership the hook created or found, empty when nothing matches
     */
    private Optional<MembershipRef> bootstrap(final SecurityIdentity identity, final IdentityKey key) {
        final JsonWebToken token = (JsonWebToken) identity.getPrincipal();
        if (!isTrue(token.getClaim("email_verified"))) {
            return Optional.empty();
        }
        final String email = token.getClaim("email");
        if (isBlank(email)) {
            return Optional.empty();
        }
        try {
            return bootstrap.get().link(key, email);
        } catch (final RuntimeException e) {
            logger.warn("Identity bootstrap failed, principal stays NOT_LINKED", e);
            return Optional.empty();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Parses a UUID without throwing.
     *
     * @param value the text to parse
     * @return the UUID, empty when the text is not one
     */
    private static Optional<UUID> parse(final String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Reads a boolean claim that an IdP may send as a boolean or as the text "true".
     *
     * @param claim the claim value
     * @return true only for boolean true or the text "true"
     */
    private static boolean isTrue(final Object claim) {
        return Boolean.TRUE.equals(claim) || claim instanceof String text && "true".equalsIgnoreCase(text);
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
