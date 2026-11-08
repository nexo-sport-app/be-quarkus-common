package it.nexoapp.common.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plain unit tests of the identity model invariants (ROL-03, D-01): identity is issuer plus subject only,
 * LINKED principals always carry association and member, the other states carry neither.
 */
class NexoPrincipalTest {

    private static final IdentityKey KEY = new IdentityKey("https://idp.example/realm", "sub-1");

    private static final UUID ASSOCIATION = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID MEMBER = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // -----------------------------------------------------------------------------------------------------------------

    /** Null or blank issuer or subject are rejected. */
    @Test
    void identityKeyRejectsNullOrBlankParts() {
        assertThrows(IllegalArgumentException.class, () -> new IdentityKey(null, "s"));
        assertThrows(IllegalArgumentException.class, () -> new IdentityKey("  ", "s"));
        assertThrows(IllegalArgumentException.class, () -> new IdentityKey("i", null));
        assertThrows(IllegalArgumentException.class, () -> new IdentityKey("i", ""));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** Null association or member are rejected. */
    @Test
    void membershipRefRejectsNullIds() {
        assertThrows(IllegalArgumentException.class, () -> new MembershipRef(null, MEMBER, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new MembershipRef(ASSOCIATION, null, Set.of()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** A null role set becomes empty. */
    @Test
    void membershipRefNullRolesBecomeEmpty() {
        final MembershipRef ref = new MembershipRef(ASSOCIATION, MEMBER, null);
        assertTrue(ref.roles().isEmpty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** The role set is immutable and detached from the caller's set. */
    @Test
    void membershipRefRolesAreImmutableDefensiveCopy() {
        final Set<Role> source = new HashSet<>(Set.of(Role.COACH));
        final MembershipRef ref = new MembershipRef(ASSOCIATION, MEMBER, source);
        assertThrows(UnsupportedOperationException.class, () -> ref.roles().add(Role.PRESIDENT));
        source.add(Role.TREASURER);
        assertEquals(Set.of(Role.COACH), ref.roles());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** linked copies association, member and roles from the membership. */
    @Test
    void linkedCopiesMembership() {
        final MembershipRef ref = new MembershipRef(ASSOCIATION, MEMBER, EnumSet.of(Role.PRESIDENT, Role.ATHLETE));
        final NexoPrincipal principal = NexoPrincipal.linked(KEY, ref);
        assertEquals(NexoPrincipal.LinkState.LINKED, principal.state());
        assertEquals(KEY, principal.key());
        assertEquals(ASSOCIATION, principal.associationId());
        assertEquals(MEMBER, principal.memberId());
        assertEquals(Set.of(Role.PRESIDENT, Role.ATHLETE), principal.roles());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** notLinked and ambiguous carry no association, member or role. */
    @Test
    void notLinkedAndAmbiguousCarryNothing() {
        for (final NexoPrincipal principal : List.of(NexoPrincipal.notLinked(KEY), NexoPrincipal.ambiguous(KEY))) {
            assertNull(principal.associationId());
            assertNull(principal.memberId());
            assertTrue(principal.roles().isEmpty());
            assertEquals(KEY, principal.key());
        }
        assertEquals(NexoPrincipal.LinkState.NOT_LINKED, NexoPrincipal.notLinked(KEY).state());
        assertEquals(NexoPrincipal.LinkState.AMBIGUOUS, NexoPrincipal.ambiguous(KEY).state());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** A LINKED principal without association or member is rejected. */
    @Test
    void linkedRequiresAssociationAndMember() {
        assertThrows(IllegalArgumentException.class,
                () -> new NexoPrincipal(NexoPrincipal.LinkState.LINKED, KEY, null, MEMBER, Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new NexoPrincipal(NexoPrincipal.LinkState.LINKED, KEY, ASSOCIATION, null, Set.of()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** NOT_LINKED and AMBIGUOUS with roles, association or member are rejected. */
    @Test
    void unlinkedStatesRejectRolesAndIds() {
        for (final NexoPrincipal.LinkState state : List.of(NexoPrincipal.LinkState.NOT_LINKED, NexoPrincipal.LinkState.AMBIGUOUS)) {
            assertThrows(IllegalArgumentException.class,
                    () -> new NexoPrincipal(state, KEY, null, null, Set.of(Role.COACH)));
            assertThrows(IllegalArgumentException.class,
                    () -> new NexoPrincipal(state, KEY, ASSOCIATION, null, Set.of()));
            assertThrows(IllegalArgumentException.class,
                    () -> new NexoPrincipal(state, KEY, null, MEMBER, Set.of()));
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** State and key are mandatory. */
    @Test
    void stateAndKeyAreMandatory() {
        assertThrows(IllegalArgumentException.class, () -> new NexoPrincipal(null, KEY, null, null, Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new NexoPrincipal(NexoPrincipal.LinkState.NOT_LINKED, null, null, null, Set.of()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** The principal roles are an immutable copy as well. */
    @Test
    void principalRolesAreImmutable() {
        final NexoPrincipal principal = NexoPrincipal.linked(KEY, new MembershipRef(ASSOCIATION, MEMBER, Set.of(Role.COACH)));
        assertThrows(UnsupportedOperationException.class, () -> principal.roles().add(Role.PRESIDENT));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** The record components are exactly the declared ones: no personal data component exists. */
    @Test
    void recordComponentsAreExactlyTheDeclaredOnes() {
        assertEquals(List.of("issuer", "subject"), componentNames(IdentityKey.class));
        assertEquals(List.of("associationId", "memberId", "roles"), componentNames(MembershipRef.class));
        assertEquals(List.of("state", "key", "associationId", "memberId", "roles"), componentNames(NexoPrincipal.class));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** The attribute name under which the principal is attached to the security identity. */
    @Test
    void attributeNameIsStable() {
        assertEquals("nexo.principal", NexoPrincipal.ATTRIBUTE);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static List<String> componentNames(final Class<? extends Record> type) {
        return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
    }

}
