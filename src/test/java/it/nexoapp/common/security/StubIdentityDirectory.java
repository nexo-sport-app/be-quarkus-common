package it.nexoapp.common.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test double of the service-local identity directory: memberships are held in memory per identity and tests
 * set them up before issuing a request.
 */
@Alternative
@Priority(1)
@ApplicationScoped
class StubIdentityDirectory implements IdentityDirectory {

    /** Issuer used by the test identities. */
    static final String ISSUER = "https://idp.nexo.test/realm";

    /* - state - */
    /** Memberships per identity, mutable by tests. */
    private final Map<IdentityKey, List<MembershipRef>> memberships = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MembershipRef> memberships(final IdentityKey key) {
        return memberships.getOrDefault(key, List.of());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Sets the memberships of a test subject, issued by {@link #ISSUER}.
     *
     * @param subject     the subject of the test identity
     * @param memberships the memberships the directory returns for it
     */
    void put(final String subject, final MembershipRef... memberships) {
        this.memberships.put(new IdentityKey(ISSUER, subject), List.of(memberships));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Forgets every membership.
     */
    void clear() {
        memberships.clear();
    }

}
