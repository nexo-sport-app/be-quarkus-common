package it.nexoapp.common.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.response.Response;
import it.nexoapp.common.constant.Headers;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Behaviour of the identity gate and of the role-denial mapping through real HTTP requests with test OIDC
 * identities (ROL-02, D-01, D-04).
 */
@QuarkusTest
class IdentityGateTest {

    private static final UUID ASSOCIATION_A = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

    private static final UUID MEMBER_A = UUID.fromString("a1a1a1a1-a1a1-41a1-81a1-a1a1a1a1a1a1");

    private static final UUID ASSOCIATION_B = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");

    private static final UUID MEMBER_B = UUID.fromString("c1c1c1c1-c1c1-41c1-81c1-c1c1c1c1c1c1");

    private static final UUID UNRELATED = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");

    /** What every unlinked caller must receive, whatever the state of its email claim. */
    private static final Denial UNLINKED_DENIAL = new Denial(403, IdentityNotLinkedException.CODE, IdentityNotLinkedException.MESSAGE);

    /* - collaborators - */
    /** Directory stub the augmentor resolves through. */
    @Inject
    StubIdentityDirectory directory;

    /** Bootstrap stub counting its calls. */
    @Inject
    StubIdentityBootstrap bootstrap;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The observable part of a denial: status, code and message.
     *
     * @param status  HTTP status of the response
     * @param code    Fault code
     * @param message Fault message
     */
    private record Denial(int status, String code, String message) {
    }

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
     * Extracts status, code and message of a response.
     *
     * @param response the response of a denied request
     * @return its observable denial
     */
    private static Denial denial(final Response response) {
        return new Denial(response.statusCode(), response.jsonPath().getString("code"), response.jsonPath().getString("message"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A linked caller reaches an endpoint without role annotations and sees its association.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-linked"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void linkedCallerReachesAuthenticatedEndpoint() {
        directory.put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        given().when().get("/probe-security/authenticated").then()
                .statusCode(200)
                .body("associationId", equalTo(ASSOCIATION_A.toString()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unlinked caller is stopped with IDENTITY_NOT_LINKED on an endpoint without role annotations.
     */
    @Test
    @TestSecurity(user = "sub-stranger", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-stranger"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void unlinkedCallerIsStoppedOnAuthenticatedEndpoint() {
        given().when().get("/probe-security/authenticated").then()
                .statusCode(403)
                .body("code", equalTo("IDENTITY_NOT_LINKED"))
                .body("message", equalTo(IdentityNotLinkedException.MESSAGE));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unlinked caller reaches an endpoint annotated with AllowUnlinked and has no association.
     */
    @Test
    @TestSecurity(user = "sub-stranger", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-stranger"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void unlinkedCallerReachesAllowUnlinkedEndpoint() {
        given().when().get("/probe-security/me").then()
                .statusCode(200)
                .body("associationId", nullValue());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An ambiguous caller reaches an AllowUnlinked endpoint, so it can list its associations.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void ambiguousCallerReachesAllowUnlinkedEndpoint() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().when().get("/probe-security/me").then()
                .statusCode(200)
                .body("associationId", nullValue());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A caller with several memberships and no header must select one.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void ambiguousCallerMustSelectAnAssociation() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().when().get("/probe-security/authenticated").then()
                .statusCode(403)
                .body("code", equalTo("ASSOCIATION_SELECTION_REQUIRED"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A header naming the second association selects it and the request passes.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void headerSelectsTheSecondAssociation() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().header(Headers.ASSOCIATION_ID, ASSOCIATION_B.toString()).when().get("/probe-security/authenticated").then()
                .statusCode(200)
                .body("associationId", equalTo(ASSOCIATION_B.toString()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A header naming an association the caller is not in is still a missing selection.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void foreignHeaderIsStillASelectionError() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().header(Headers.ASSOCIATION_ID, UNRELATED.toString()).when().get("/probe-security/authenticated").then()
                .statusCode(403)
                .body("code", equalTo("ASSOCIATION_SELECTION_REQUIRED"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A linked PRESIDENT reaches the role-protected endpoint.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-linked"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void linkedPresidentReachesRoleProtectedEndpoint() {
        directory.put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        given().when().get("/probe-security/president").then()
                .statusCode(200)
                .body("associationId", equalTo(ASSOCIATION_A.toString()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unlinked caller on a role-protected endpoint gets IDENTITY_NOT_LINKED, not FORBIDDEN.
     */
    @Test
    @TestSecurity(user = "sub-stranger", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-stranger"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void unlinkedCallerOnRoleProtectedEndpointGetsNotLinked() {
        given().when().get("/probe-security/president").then()
                .statusCode(403)
                .body("code", equalTo("IDENTITY_NOT_LINKED"))
                .body("message", equalTo(IdentityNotLinkedException.MESSAGE));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A linked caller lacking the role gets FORBIDDEN, distinguishable from the unlinked case.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-linked"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void linkedCoachOnPresidentEndpointGetsForbidden() {
        directory.put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.COACH)));

        given().when().get("/probe-security/president").then()
                .statusCode(403)
                .body("code", equalTo("FORBIDDEN"))
                .body("trace", notNullValue())
                .body("origin", notNullValue())
                .body("timestamp", notNullValue());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An ambiguous caller on a role-protected endpoint gets ASSOCIATION_SELECTION_REQUIRED.
     */
    @Test
    @TestSecurity(user = "sub-two", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-two"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void ambiguousCallerOnRoleProtectedEndpointMustSelect() {
        directory.put("sub-two", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)),
                new MembershipRef(ASSOCIATION_B, MEMBER_B, Set.of(Role.COACH)));

        given().when().get("/probe-security/president").then()
                .statusCode(403)
                .body("code", equalTo("ASSOCIATION_SELECTION_REQUIRED"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * No-oracle check: a verified email the bootstrap does not recognise is denied exactly like any unlinked caller.
     */
    @Test
    @TestSecurity(user = "sub-bootstrap", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-bootstrap"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test"),
            @Claim(key = "email_verified", value = "true")
    })
    void verifiedUnmatchedEmailGetsTheStandardDenial() {
        assertEquals(UNLINKED_DENIAL, denial(given().when().get("/probe-security/president").then().extract().response()));
        assertEquals(UNLINKED_DENIAL, denial(given().when().get("/probe-security/authenticated").then().extract().response()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * No-oracle check: an unverified email gets the identical status, code and message.
     */
    @Test
    @TestSecurity(user = "sub-bootstrap", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-bootstrap"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test"),
            @Claim(key = "email_verified", value = "false")
    })
    void unverifiedEmailGetsTheSameDenial() {
        assertEquals(UNLINKED_DENIAL, denial(given().when().get("/probe-security/president").then().extract().response()));
        assertEquals(UNLINKED_DENIAL, denial(given().when().get("/probe-security/authenticated").then().extract().response()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * No-oracle check: an identity without any verification claim gets the identical denial as well.
     */
    @Test
    @TestSecurity(user = "sub-bootstrap", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = StubIdentityDirectory.ISSUER),
            @Claim(key = "sub", value = "sub-bootstrap"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void missingVerificationClaimGetsTheSameDenial() {
        assertEquals(UNLINKED_DENIAL, denial(given().when().get("/probe-security/president").then().extract().response()));
        assertEquals(UNLINKED_DENIAL, denial(given().when().get("/probe-security/authenticated").then().extract().response()));
    }

}
