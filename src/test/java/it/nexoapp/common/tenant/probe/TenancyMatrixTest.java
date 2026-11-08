package it.nexoapp.common.tenant.probe;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.nexoapp.common.context.ExecutionScope;
import it.nexoapp.common.tenant.TenantContext;
import it.nexoapp.common.tenant.TenantNotResolvedException;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tenancy matrix on a real Postgres: the discriminator column, the fail-closed resolver and the
 * unscoped escape hatch, driven through the probe table of the common test application.
 */
@QuarkusTest
class TenancyMatrixTest {

    /* - repository - */
    @Inject
    ProbeRepository repository;

    /* - entity manager - */
    @Inject
    EntityManager entityManager;

    /* - fixtures - */
    private static final UUID TENANT_A = UUID.randomUUID();
    private static final UUID TENANT_B = UUID.randomUUID();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Removes the rows of both tenants after each test.
     */
    @AfterEach
    void cleanUp() {
        for (final UUID tenant : new UUID[]{TENANT_A, TENANT_B}) {
            TenantContext.runAs(tenant, () -> QuarkusTransaction.requiringNew().run(() -> repository.deleteAll()));
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a probe row under a tenant, the tenant scope is set before the transaction starts.
     *
     * @param tenant   tenant of the row
     * @param taxCode  tax code of the row
     * @return the identifier of the new row
     */
    private UUID create(final UUID tenant, final String taxCode) {
        return TenantContext.callAs(tenant, () -> QuarkusTransaction.requiringNew().call(() -> {
            final ProbeEntity probe = ProbeEntity.builder().taxCode(taxCode).lastName("Rossi").build();
            repository.persist(probe);
            return probe.getId();
        }));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tracer: a tenant-scoped write lands with the right tenant and the right actor, a foreign tenant cannot see it,
     * and a session without a tenant fails closed.
     *
     * @throws Exception when the executor fails
     */
    @Test
    void tenantScopedWriteLandsWithTenantAndActorAndIsInvisibleToAForeignTenant() throws Exception {
        final String taxCode = UUID.randomUUID().toString().substring(0, 16);
        final UUID id = ExecutionScope.callAs("system:cmn", () -> create(TENANT_A, taxCode));

        final Object[] row = TenantContext.callAs(TENANT_A, () -> QuarkusTransaction.requiringNew().call(() ->
                (Object[]) entityManager.createNativeQuery(
                                "select tenant_id::text, created_by from probe.probe where id = :id")
                        .setParameter("id", id).getSingleResult()));
        assertEquals(TENANT_A.toString(), row[0], "tenant_id of the stored row");
        assertEquals("system:cmn", row[1], "created_by of the stored row");

        assertNull(TenantContext.callAs(TENANT_B, () -> QuarkusTransaction.requiringNew().call(() ->
                repository.findById(id))), "findById under a foreign tenant");
        assertNotNull(TenantContext.callAs(TENANT_A, () -> QuarkusTransaction.requiringNew().call(() ->
                repository.findById(id))), "findById under the owning tenant");

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> QuarkusTransaction.requiringNew().call(() -> repository.count())).get();
            fail("a session without a tenant must fail");
        } catch (final ExecutionException e) {
            if (!hasCause(e, HibernateException.class)) {
                throw new AssertionError("session refusal expected in the cause chain", e);
            }
            assertTrue(hasMessage(e, "no tenant identifier"), "Hibernate refuses a session without a tenant");
        } finally {
            executor.shutdownNow();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * With the request context active (as in every HTTP request) and no tenant scope, the resolver itself refuses
     * the session with {@link TenantNotResolvedException}: there is no default tenant.
     */
    @Test
    void activeRequestContextWithoutScopeThrowsTenantNotResolved() {
        final Throwable thrown = org.junit.jupiter.api.Assertions.assertThrows(Throwable.class,
                () -> QuarkusTransaction.requiringNew().call(() -> repository.count()));
        assertTrue(hasCause(thrown, TenantNotResolvedException.class), "TenantNotResolvedException in the cause chain");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Checks whether a message appears anywhere in the cause chain.
     *
     * @param throwable throwable to inspect
     * @param fragment  searched message fragment
     * @return true when found
     */
    private static boolean hasMessage(final Throwable throwable, final String fragment) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Checks whether a throwable has a given type in its cause chain.
     *
     * @param throwable throwable to inspect
     * @param type      searched type
     * @return true when found
     */
    private static boolean hasCause(final Throwable throwable, final Class<? extends Throwable> type) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

}
