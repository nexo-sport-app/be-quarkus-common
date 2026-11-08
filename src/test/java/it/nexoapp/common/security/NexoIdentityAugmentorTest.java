package it.nexoapp.common.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.smallrye.mutiny.Uni;
import it.nexoapp.common.constant.Headers;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behaviour of the identity augmentor through real HTTP requests with test OIDC identities (ROL-03, D-01, D-03
 * amended 2026-11-08, D-06), plus direct calls for the identities that cannot be produced over HTTP.
 */
@QuarkusTest
class NexoIdentityAugmentorTest {

    private static final String PROBE = "/probe-security/principal";

    private static final UUID ASSOCIATION_A = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

    private static final UUID MEMBER_A = UUID.fromString("a1a1a1a1-a1a1-41a1-81a1-a1a1a1a1a1a1");

    private static final UUID ASSOCIATION_B = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");

    private static final UUID MEMBER_B = UUID.fromString("c1c1c1c1-c1c1-41c1-81c1-c1c1c1c1c1c1");

    private static final UUID UNRELATED = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");

    /** Request context whose blocking work runs synchronously. */
    private static final AuthenticationRequestContext SYNCHRONOUS = supplier -> Uni.createFrom().item(supplier.get());

    /* - collaborators - */
    /** Directory stub the augmentor resolves through. */
    @Inject
    StubIdentityDirectory directory;

    /** Bootstrap stub counting its calls. */
    @Inject
    StubIdentityBootstrap bootstrap;

    /** Augmentor under test, called directly in the non-HTTP cases. */
    @Inject
    NexoIdentityAugmentor augmentor;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Starts each test with an empty directory and zeroed counters.
     */
    @BeforeEach
    void reset() {
        directory.clear();
        bootstrap.reset();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An identity with one membership reaches the resource as LINKED with the directory's roles.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-linked")
    })
    void oneMembershipIsLinkedWithItsRoles() {
        directory.put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("LINKED"))
                .body("associationId", equalTo(ASSOCIATION_A.toString()))
                .body("memberId", equalTo(MEMBER_A.toString()))
                .body("roles", containsInAnyOrder("PRESIDENT"))
                .body("identityRoles", containsInAnyOrder("PRESIDENT"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An identity unknown to the directory is NOT_LINKED and carries no roles.
     */
    @Test
    @TestSecurity(user = "sub-stranger", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-stranger")
    })
    void unknownIdentityIsNotLinked() {
        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("NOT_LINKED"))
                .body("associationId", nullValue())
                .body("roles", empty())
                .body("identityRoles", empty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Several memberships and no header leave the principal AMBIGUOUS without roles.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two")
    })
    void twoMembershipsWithoutHeaderAreAmbiguous() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("AMBIGUOUS"))
                .body("associationId", nullValue())
                .body("identityRoles", empty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The header selects the named membership and only its roles are added.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two")
    })
    void headerSelectsTheNamedMembership() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().header(Headers.ASSOCIATION_ID, ASSOCIATION_B.toString()).when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("LINKED"))
                .body("associationId", equalTo(ASSOCIATION_B.toString()))
                .body("memberId", equalTo(MEMBER_B.toString()))
                .body("roles", containsInAnyOrder("COACH"))
                .body("identityRoles", containsInAnyOrder("COACH"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A header naming an association the caller does not belong to selects nothing.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two")
    })
    void headerWithUnrelatedAssociationIsAmbiguous() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().header(Headers.ASSOCIATION_ID, UNRELATED.toString()).when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("AMBIGUOUS"))
                .body("identityRoles", empty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A header that is not a UUID selects nothing.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two")
    })
    void malformedHeaderIsAmbiguous() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().header(Headers.ASSOCIATION_ID, "not-a-uuid").when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("AMBIGUOUS"))
                .body("identityRoles", empty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The header is never trusted alone: a single membership plus a header naming another association is AMBIGUOUS.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-linked")
    })
    void singleMembershipWithForeignHeaderIsAmbiguous() {
        directory.put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        given().header(Headers.ASSOCIATION_ID, ASSOCIATION_B.toString()).when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("AMBIGUOUS"))
                .body("associationId", nullValue())
                .body("identityRoles", empty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A header naming the caller's only membership leaves it LINKED.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-linked")
    })
    void singleMembershipWithMatchingHeaderIsLinked() {
        directory.put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        given().header(Headers.ASSOCIATION_ID, ASSOCIATION_A.toString()).when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("LINKED"))
                .body("associationId", equalTo(ASSOCIATION_A.toString()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A header cannot link an identity that has no membership.
     */
    @Test
    @TestSecurity(user = "sub-stranger", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-stranger")
    })
    void headerDoesNotLinkAnIdentityWithoutMembership() {
        given().header(Headers.ASSOCIATION_ID, ASSOCIATION_A.toString()).when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("NOT_LINKED"))
                .body("identityRoles", empty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unlinked identity with a verified, matching email is linked by the bootstrap hook exactly once.
     */
    @Test
    @TestSecurity(user = "sub-bootstrap", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-bootstrap"),
            @Claim(key = "email", value = "bootstrap@nexo.test"),
            @Claim(key = "email_verified", value = "true")
    })
    void verifiedEmailLinksAnUnlinkedIdentityOnce() {
        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("LINKED"))
                .body("associationId", equalTo(StubIdentityBootstrap.ASSOCIATION.toString()))
                .body("memberId", equalTo(StubIdentityBootstrap.MEMBER.toString()))
                .body("identityRoles", containsInAnyOrder("PRESIDENT"));

        assertEquals(1, bootstrap.callsFor("sub-bootstrap"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unverified email is ignored: the hook is not called and the identity stays NOT_LINKED.
     */
    @Test
    @TestSecurity(user = "sub-bootstrap", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-bootstrap"),
            @Claim(key = "email", value = "bootstrap@nexo.test"),
            @Claim(key = "email_verified", value = "false")
    })
    void unverifiedEmailNeverReachesTheBootstrap() {
        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("NOT_LINKED"));

        assertEquals(0, bootstrap.callsFor("sub-bootstrap"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An email without the email_verified claim is ignored.
     */
    @Test
    @TestSecurity(user = "sub-bootstrap", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-bootstrap"),
            @Claim(key = "email", value = "bootstrap@nexo.test")
    })
    void missingVerificationClaimNeverReachesTheBootstrap() {
        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("NOT_LINKED"));

        assertEquals(0, bootstrap.callsFor("sub-bootstrap"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A verified email the hook does not recognise leaves the identity NOT_LINKED.
     */
    @Test
    @TestSecurity(user = "sub-bootstrap", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-bootstrap"),
            @Claim(key = "email_verified", value = "true"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void verifiedEmailThatMatchesNobodyStaysNotLinked() {
        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("NOT_LINKED"));

        assertEquals(1, bootstrap.callsFor("sub-bootstrap"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Once linked, the email claim is not read again: the hook is never called for a linked subject.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-linked"),
            @Claim(key = "email", value = "bootstrap@nexo.test"),
            @Claim(key = "email_verified", value = "true")
    })
    void linkedIdentityNeverReachesTheBootstrap() {
        directory.put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("LINKED"));

        assertEquals(0, bootstrap.callsFor("sub-linked"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An identity with several memberships is linked already: the hook is not called.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two"),
            @Claim(key = "email", value = "bootstrap@nexo.test"),
            @Claim(key = "email_verified", value = "true")
    })
    void ambiguousIdentityNeverReachesTheBootstrap() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("AMBIGUOUS"));

        assertEquals(0, bootstrap.callsFor("sub-two"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An anonymous identity passes through unchanged and carries no principal.
     */
    @Test
    void anonymousIdentityPassesThroughUnchanged() {
        final SecurityIdentity anonymous = QuarkusSecurityIdentity.builder().setAnonymous(true).build();

        final SecurityIdentity result = augmentor.augment(anonymous, SYNCHRONOUS).await().indefinitely();

        assertSame(anonymous, result);
        assertNull(result.getAttribute(NexoPrincipal.ATTRIBUTE));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A principal that is not a JWT is NOT_LINKED, and the augmentor does not throw.
     */
    @Test
    void nonJwtPrincipalIsNotLinked() {
        final SecurityIdentity basic = QuarkusSecurityIdentity.builder().setPrincipal(new QuarkusPrincipal("someone")).build();

        final SecurityIdentity result = augmentor.augment(basic, SYNCHRONOUS).await().indefinitely();

        final NexoPrincipal principal = result.getAttribute(NexoPrincipal.ATTRIBUTE);
        assertEquals(NexoPrincipal.LinkState.NOT_LINKED, principal.state());
        assertTrue(result.getRoles().isEmpty());
        assertEquals(0, bootstrap.callsFor("someone"));
    }

}
