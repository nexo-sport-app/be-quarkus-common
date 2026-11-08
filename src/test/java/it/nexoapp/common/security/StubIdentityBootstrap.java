package it.nexoapp.common.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test double of the first-login hook: counts the calls per subject and links the subject
 * {@link #SUBJECT} only when it is given {@link #EMAIL}.
 */
@Alternative
@Priority(1)
@ApplicationScoped
class StubIdentityBootstrap implements IdentityBootstrap {

    /** The only subject that gets linked. */
    static final String SUBJECT = "sub-bootstrap";

    /** The only email that links {@link #SUBJECT}. */
    static final String EMAIL = "bootstrap@nexo.test";

    /** Association of the membership the stub creates. */
    static final UUID ASSOCIATION = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");

    /** Member of the membership the stub creates. */
    static final UUID MEMBER = UUID.fromString("b1b1b1b1-b1b1-41b1-81b1-b1b1b1b1b1b1");

    /* - state - */
    /** Number of calls per subject. */
    private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MembershipRef> link(final IdentityKey key, final String verifiedEmail) {
        calls.computeIfAbsent(key.subject(), subject -> new AtomicInteger()).incrementAndGet();
        if (SUBJECT.equals(key.subject()) && EMAIL.equals(verifiedEmail)) {
            return Optional.of(new MembershipRef(ASSOCIATION, MEMBER, Set.of(Role.PRESIDENT)));
        }
        return Optional.empty();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns how often the hook was called for a subject.
     *
     * @param subject the subject of the test identity
     * @return the number of calls since the last reset
     */
    int callsFor(final String subject) {
        final AtomicInteger counter = calls.get(subject);
        return counter == null ? 0 : counter.get();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Resets every counter.
     */
    void reset() {
        calls.clear();
    }

}
