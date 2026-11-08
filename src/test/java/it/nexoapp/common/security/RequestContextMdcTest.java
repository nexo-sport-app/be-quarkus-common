package it.nexoapp.common.security;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.context.RequestContextOutFilter;
import it.nexoapp.common.tenant.TenantContext;
import it.nexoapp.common.tenant.probe.ProbeEntity;
import it.nexoapp.common.tenant.probe.ProbeRepository;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The request context and the MDC hold only identifiers taken from the resolved principal, are cleared after
 * each request, and the request's association becomes the Hibernate tenant (D-20, ROL-02, ROL-03).
 */
@QuarkusTest
class RequestContextMdcTest {

    /** Issuer used by the test identities. */
    private static final String ISSUER = StubIdentityDirectory.ISSUER;

    private static final UUID ASSOCIATION_A = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

    private static final UUID MEMBER_A = UUID.fromString("a1a1a1a1-a1a1-41a1-81a1-a1a1a1a1a1a1");

    private static final UUID ASSOCIATION_B = UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");

    /* - collaborators - */
    /** Directory stub the augmentor resolves through. */
    @Inject
    StubIdentityDirectory directory;

    /** Probe table. */
    @Inject
    ProbeRepository repository;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Starts each test with an empty directory.
     */
    @BeforeEach
    void reset() {
        clearDirectory();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Removes the probe rows and the directory entries created by a test.
     */
    @AfterEach
    void cleanUp() {
        for (final UUID tenant : new UUID[]{ASSOCIATION_A, ASSOCIATION_B}) {
            TenantContext.runAs(tenant, () -> QuarkusTransaction.requiringNew().run(() -> repository.deleteAll()));
        }
        clearDirectory();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Forgets every membership of the stub directory.
     */
    private void clearDirectory() {
        directory.clear();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Seeds a probe row under an explicit tenant scope.
     *
     * @param tenant tenant that owns the row
     */
    private void seed(final UUID tenant) {
        TenantContext.runAs(tenant, () -> QuarkusTransaction.requiringNew().run(() -> repository.persist(
                ProbeEntity.builder().taxCode(UUID.randomUUID().toString().substring(0, 16)).lastName("Rossi").build())));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A linked request sees its association, member and subject in the MDC and the request context, with the
     * call id of the header.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = ISSUER),
            @Claim(key = "sub", value = "sub-linked")
    })
    void linkedRequestCarriesIdsInMdcAndRequestContext() {
        put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        given().header(Headers.CALL_ID, "call-123").when().get("/probe-security/mdc").then()
                .statusCode(200)
                .header(Headers.CALL_ID, equalTo("call-123"))
                .body("callId", equalTo("call-123"))
                .body("associationId", equalTo(ASSOCIATION_A.toString()))
                .body("memberId", equalTo(MEMBER_A.toString()))
                .body("subject", equalTo("sub-linked"))
                .body("contextAssociationId", equalTo(ASSOCIATION_A.toString()))
                .body("contextMemberId", equalTo(MEMBER_A.toString()));

        assertEquals("call-123", MdcSpyFilter.LAST_CALL_ID.get(), "the MDC is still filled while other response filters run");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Without a call id header a UUID is generated and returned in the response.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = ISSUER),
            @Claim(key = "sub", value = "sub-linked")
    })
    void missingCallIdIsGeneratedAndReturned() {
        put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        final String callId = given().when().get("/probe-security/mdc").then()
                .statusCode(200)
                .header(Headers.CALL_ID, notNullValue())
                .extract().jsonPath().getString("callId");

        assertNotNull(callId);
        assertEquals(callId, UUID.fromString(callId).toString(), "the generated call id is a UUID");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unlinked request has the subject only: no association and no member anywhere.
     */
    @Test
    @TestSecurity(user = "sub-stranger", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = ISSUER),
            @Claim(key = "sub", value = "sub-stranger")
    })
    void unlinkedRequestHasNoAssociationNorMember() {
        given().when().get("/probe-security/mdc").then()
                .statusCode(200)
                .body("associationId", nullValue())
                .body("memberId", nullValue())
                .body("contextAssociationId", nullValue())
                .body("contextMemberId", nullValue())
                .body("subject", equalTo("sub-stranger"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A request that follows a linked one on the same worker thread does not inherit its association or member.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = ISSUER),
            @Claim(key = "sub", value = "sub-linked")
    })
    void consecutiveRequestsNeverInheritEachOthersIds() {
        put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));

        for (int i = 0; i < 5; i++) {
            given().when().get("/probe-security/mdc").then()
                    .statusCode(200)
                    .body("associationId", equalTo(ASSOCIATION_A.toString()));
        }
        // a header naming a foreign association makes the principal AMBIGUOUS: the MDC must not keep the old ids
        given().header(Headers.ASSOCIATION_ID, ASSOCIATION_B.toString()).when().get("/probe-security/mdc").then()
                .statusCode(200)
                .body("associationId", nullValue())
                .body("memberId", nullValue());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The out filter adds the call id to the response and clears every per-request MDC key.
     */
    @Test
    void outFilterClearsThePerRequestMdcKeys() {
        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        final ContainerResponseContext response = (ContainerResponseContext) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{ContainerResponseContext.class},
                (proxy, method, args) -> "getHeaders".equals(method.getName()) ? headers : null);
        MDCUtils.put(MDCConstant.CALL_ID, "call-xyz");
        MDCUtils.put(MDCConstant.ASSOCIATION_ID, ASSOCIATION_A.toString());
        MDCUtils.put(MDCConstant.MEMBER_ID, MEMBER_A.toString());
        MDCUtils.put(MDCConstant.IP, "127.0.0.1");
        MDCUtils.put(MDCConstant.CALLER, "WEB");
        MDCUtils.put(MDCConstant.APP_VERSION, "1.0.0");
        try {
            new RequestContextOutFilter().filter(null, response);

            assertEquals("call-xyz", headers.getFirst(Headers.CALL_ID), "response carries the call id");
            for (final MDCConstant key : new MDCConstant[]{MDCConstant.CALL_ID, MDCConstant.ASSOCIATION_ID,
                    MDCConstant.MEMBER_ID, MDCConstant.IP, MDCConstant.CALLER, MDCConstant.APP_VERSION}) {
                assertNull(MDCUtils.get(key), key + " must be cleared");
            }
        } finally {
            MDC.clear();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A linked request counts only the rows of its own association.
     */
    @Test
    @TestSecurity(user = "sub-linked", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = ISSUER),
            @Claim(key = "sub", value = "sub-linked")
    })
    void linkedRequestIsScopedToItsAssociation() {
        put("sub-linked", new MembershipRef(ASSOCIATION_A, MEMBER_A, Set.of(Role.PRESIDENT)));
        seed(ASSOCIATION_A);
        seed(ASSOCIATION_B);

        given().when().get("/probe-security/probe-count").then()
                .statusCode(200)
                .body("count", equalTo(1));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unlinked request touching a tenant table fails closed, with no default tenant.
     */
    @Test
    @TestSecurity(user = "sub-stranger", augmentors = NexoIdentityAugmentor.class)
    @OidcSecurity(claims = {
            @Claim(key = "iss", value = ISSUER),
            @Claim(key = "sub", value = "sub-stranger")
    })
    void unlinkedRequestTouchingTenantDataFailsClosed() {
        seed(ASSOCIATION_A);

        given().when().get("/probe-security/probe-count").then()
                .statusCode(403)
                .body("code", equalTo("TENANT_NOT_RESOLVED"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Sets the memberships of a test subject in the stub directory.
     *
     * @param subject     the subject of the test identity
     * @param memberships memberships the directory returns for it
     */
    private void put(final String subject, final MembershipRef... memberships) {
        directory.put(subject, memberships);
    }

}
