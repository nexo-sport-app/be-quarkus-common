package it.nexoapp.common.tenant.probe;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.test.junit.QuarkusTest;
import it.nexoapp.common.context.ExecutionScope;
import it.nexoapp.common.tenant.TenantContext;
import it.nexoapp.common.tenant.TenantNotResolvedException;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Runs a body in a new transaction under a tenant scope that is opened before the transaction.
     *
     * @param <T>    type of the result
     * @param tenant tenant of the scope
     * @param body   body to run
     * @return the result of the body
     */
    private <T> T inTenant(final UUID tenant, final Callable<T> body) {
        return TenantContext.callAs(tenant, () -> QuarkusTransaction.requiringNew().call(body));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Generates a random unique tax code.
     *
     * @return a 16 character key
     */
    private static String key() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Queries and bulk statements under a foreign tenant see and touch no row of the owning tenant.
     */
    @Test
    void foreignTenantSeesAndTouchesNothing() {
        final String taxCode = key();
        create(TENANT_A, taxCode);

        assertEquals(1L, inTenant(TENANT_A, () -> repository.count()), "count under the owner");
        assertEquals(0L, inTenant(TENANT_B, () -> repository.count()), "count under a foreign tenant");
        assertTrue(inTenant(TENANT_B, () -> repository.listAll()).isEmpty(), "listAll under a foreign tenant");
        assertTrue(inTenant(TENANT_B, () -> repository.find("taxCode", taxCode).list()).isEmpty(),
                "find under a foreign tenant");
        assertEquals(0, inTenant(TENANT_B, () -> repository.update("lastName = 'Hacked' where taxCode = ?1", taxCode)),
                "HQL update under a foreign tenant");
        assertEquals(0L, inTenant(TENANT_B, () -> repository.delete("taxCode", taxCode)),
                "HQL delete under a foreign tenant");
        assertEquals(0L, inTenant(TENANT_B, () -> repository.deleteAll()), "deleteAll under a foreign tenant");

        final List<ProbeEntity> rows = inTenant(TENANT_A, () -> repository.listAll());
        assertEquals(1, rows.size(), "the owner's row survives every foreign statement");
        assertEquals("Rossi", rows.getFirst().getLastName(), "the owner's row is untouched");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A reference to a foreign row is a proxy that fails as soon as it is used.
     */
    @Test
    void referenceToAForeignRowFailsOnAccess() {
        final UUID id = create(TENANT_A, key());

        assertThrows(EntityNotFoundException.class, () -> inTenant(TENANT_B, () ->
                entityManager.getReference(ProbeEntity.class, id).getLastName()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Native SQL is not filtered by Hibernate: it sees every tenant, which is why it is banned in repositories.
     */
    @Test
    void nativeSqlSeesBothTenants() {
        create(TENANT_A, key());
        create(TENANT_B, key());

        final Number count = inTenant(TENANT_A, () -> (Number) entityManager
                .createNativeQuery("select count(*) from probe.probe").getSingleResult());
        assertEquals(2L, count.longValue(), "native count crosses tenants");
        assertEquals(1L, inTenant(TENANT_A, () -> repository.count()), "the entity query stays scoped");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The same tax code can exist in two tenants, a duplicate inside one tenant is rejected.
     */
    @Test
    void uniqueKeyIsPerTenant() {
        final String taxCode = key();
        create(TENANT_A, taxCode);
        create(TENANT_B, taxCode);

        assertEquals(1L, inTenant(TENANT_A, () -> repository.count("taxCode", taxCode)), "one row in A");
        assertEquals(1L, inTenant(TENANT_B, () -> repository.count("taxCode", taxCode)), "one row in B");

        final Throwable duplicate = assertThrows(Throwable.class, () -> create(TENANT_A, taxCode));
        assertTrue(hasCause(duplicate, ConstraintViolationException.class), "the (tenant_id, tax_code) constraint fires");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A tenant without rows gets an empty result, never rows of another tenant.
     */
    @Test
    void tenantWithoutRowsGetsAnEmptyResult() {
        create(TENANT_A, key());
        final UUID fresh = UUID.randomUUID();

        assertTrue(inTenant(fresh, () -> repository.listAll()).isEmpty(), "listAll of a fresh tenant");
        assertEquals(0L, inTenant(fresh, () -> repository.count()), "count of a fresh tenant");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Inside an unscoped block tenant rows are invisible and cannot be written.
     */
    @Test
    void unscopedBlockSeesNoTenantRowsAndCannotWriteThem() {
        create(TENANT_A, key());

        assertEquals(0L, TenantContext.callUnscoped(() -> QuarkusTransaction.requiringNew().call(() ->
                repository.count())), "unscoped count");

        final Throwable write = assertThrows(Throwable.class, () -> TenantContext.runUnscoped(() ->
                QuarkusTransaction.requiringNew().run(() -> repository.persist(
                        ProbeEntity.builder().taxCode(key()).lastName("Rossi").build()))));
        assertTrue(hasCause(write, IllegalStateException.class), "unscoped write is rejected by the guard");
        assertEquals(1L, inTenant(TENANT_A, () -> repository.count()), "nothing was written");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A tenant id assigned by hand that differs from the current tenant is rejected.
     */
    @Test
    void manuallyAssignedForeignTenantIdIsRejected() {
        final String taxCode = key();

        assertThrows(Throwable.class, () -> inTenant(TENANT_A, () -> {
            final ProbeEntity probe = ProbeEntity.builder().taxCode(taxCode).lastName("Rossi").build();
            probe.setTenantId(TENANT_B.toString());
            repository.persist(probe);
            return probe.getId();
        }));

        assertEquals(0L, inTenant(TENANT_A, () -> repository.count("taxCode", taxCode)), "nothing stored under A");
        assertEquals(0L, inTenant(TENANT_B, () -> repository.count("taxCode", taxCode)), "nothing stored under B");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Optimistic locking keeps working under a tenant scope.
     */
    @Test
    void optimisticVersionIncrementsOnUpdate() {
        final UUID id = create(TENANT_A, key());
        final Long before = inTenant(TENANT_A, () -> repository.findById(id).getVersion());

        inTenant(TENANT_A, () -> {
            repository.findById(id).setLastName("Bianchi");
            return null;
        });

        final ProbeEntity after = inTenant(TENANT_A, () -> repository.findById(id));
        assertEquals("Bianchi", after.getLastName(), "the update is stored");
        assertEquals(before + 1, after.getVersion(), "the version is incremented");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Nested scopes restore the previous tenant, also when the inner body throws.
     */
    @Test
    void nestedScopesRestoreThePreviousTenant() {
        TenantContext.runAs(TENANT_A, () -> {
            assertEquals(TENANT_A.toString(), TenantContext.current().orElseThrow(), "outer scope");
            TenantContext.runAs(TENANT_B, () ->
                    assertEquals(TENANT_B.toString(), TenantContext.current().orElseThrow(), "inner scope"));
            assertEquals(TENANT_A.toString(), TenantContext.current().orElseThrow(), "outer scope restored");

            assertThrows(IllegalStateException.class, () -> TenantContext.runUnscoped(() -> {
                assertTrue(TenantContext.isUnscoped(), "unscoped inside the block");
                throw new IllegalStateException("boom");
            }));
            assertFalse(TenantContext.isUnscoped(), "unscoped flag cleared after the throwing block");
            assertEquals(TENANT_A.toString(), TenantContext.current().orElseThrow(), "restored after a throw");
        });
        assertTrue(TenantContext.current().isEmpty(), "no tenant left on the test thread");
        assertThrows(IllegalArgumentException.class, () -> TenantContext.runAs(null, () -> { }));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Two threads working for different tenants at the same time each see only their own rows.
     *
     * @throws Exception when the executor fails
     */
    @Test
    void parallelTenantsStayIsolated() throws Exception {
        create(TENANT_A, key());
        create(TENANT_B, key());
        create(TENANT_B, key());

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch bothInside = new CountDownLatch(2);
        try {
            final Future<Long> countA = executor.submit(() -> TenantContext.callAs(TENANT_A, () -> {
                await(bothInside);
                return QuarkusTransaction.requiringNew().call(() -> repository.count());
            }));
            final Future<Long> countB = executor.submit(() -> TenantContext.callAs(TENANT_B, () -> {
                await(bothInside);
                return QuarkusTransaction.requiringNew().call(() -> repository.count());
            }));

            assertEquals(1L, countA.get(30, TimeUnit.SECONDS), "tenant A thread");
            assertEquals(2L, countB.get(30, TimeUnit.SECONDS), "tenant B thread");
        } finally {
            executor.shutdownNow();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Signals that the calling thread is inside its scope and waits for the other one.
     *
     * @param latch latch shared by the two threads
     */
    private static void await(final CountDownLatch latch) {
        latch.countDown();
        try {
            assertTrue(latch.await(30, TimeUnit.SECONDS), "both scopes must overlap");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Regression for the stringtype=unspecified convention: String binds keep working, patterns match and a
     * digit-only String is never coerced to a number.
     */
    @Test
    void stringBindsAreUnaffectedByStringtypeUnspecified() {
        final String digits = "01234567890";
        create(TENANT_A, digits);

        final List<ProbeEntity> byKey = inTenant(TENANT_A, () -> repository.find("taxCode", digits).list());
        assertEquals(1, byKey.size(), "equality bind on a digit-only String");
        assertEquals(digits, byKey.getFirst().getTaxCode(), "the leading zero is preserved");

        final List<ProbeEntity> byPattern = inTenant(TENANT_A, () ->
                repository.find("lastName like :p", Parameters.with("p", "Ro%")).list());
        assertEquals(1, byPattern.size(), "like bind with a % pattern");
        assertEquals(1L, inTenant(TENANT_A, () -> repository.count("taxCode like ?1", "0123%")), "like on digits");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * deleteAll under one tenant removes only the rows of that tenant.
     */
    @Test
    void deleteAllRemovesOnlyTheRowsOfTheCurrentTenant() {
        create(TENANT_A, key());
        create(TENANT_B, key());

        assertEquals(1L, inTenant(TENANT_A, () -> repository.deleteAll()), "rows deleted under A");
        assertEquals(0L, inTenant(TENANT_A, () -> repository.count()), "A is empty");
        assertEquals(1L, inTenant(TENANT_B, () -> repository.count()), "B is untouched");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The tenant column and the creator survive an update unchanged.
     */
    @Test
    void tenantColumnIsNotRewrittenByAnUpdate() {
        final UUID id = ExecutionScope.callAs("system:cmn", () -> create(TENANT_A, key()));

        ExecutionScope.runAs("idp:editor", () -> inTenant(TENANT_A, () -> {
            repository.findById(id).setLastName("Verdi");
            return null;
        }));

        final Object[] row = inTenant(TENANT_A, () -> (Object[]) entityManager.createNativeQuery(
                        "select tenant_id::text, created_by, updated_by from probe.probe where id = :id")
                .setParameter("id", id).getSingleResult());
        assertEquals(TENANT_A.toString(), row[0], "tenant_id is unchanged");
        assertEquals("system:cmn", row[1], "created_by is unchanged");
        assertEquals("idp:editor", row[2], "updated_by follows the editor");
    }

}
