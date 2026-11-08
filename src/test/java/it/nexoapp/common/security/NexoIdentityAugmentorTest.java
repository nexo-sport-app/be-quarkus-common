package it.nexoapp.common.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
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

/**
 * Behaviour of the identity augmentor through real HTTP requests with test OIDC identities (ROL-03, D-01, D-06).
 */
@QuarkusTest
class NexoIdentityAugmentorTest {

    private static final String PROBE = "/probe-security/principal";

    private static final UUID ASSOCIATION_A = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

    private static final UUID MEMBER_A = UUID.fromString("a1a1a1a1-a1a1-41a1-81a1-a1a1a1a1a1a1");

    /* - directory - */
    /** Directory stub the augmentor resolves through. */
    @Inject
    StubIdentityDirectory directory;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Starts each test with an empty directory.
     */
    @BeforeEach
    void reset() {
        directory.clear();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An identity with one membership reaches the resource as LINKED with the directory's roles.
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
            @Claim(key = "sub", value = "sub-stranger"),
            // read by the current request filter until plan 01-14 rewrites it
            @Claim(key = "sid", value = "session"),
            @Claim(key = "preferred_username", value = "user"),
            @Claim(key = "email", value = "user@nexo.test")
    })
    void unknownIdentityIsNotLinked() {
        given().when().get(PROBE).then()
                .statusCode(200)
                .body("state", equalTo("NOT_LINKED"))
                .body("associationId", nullValue())
                .body("roles", empty())
                .body("identityRoles", empty());
    }

}
