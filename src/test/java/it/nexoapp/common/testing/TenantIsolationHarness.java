package it.nexoapp.common.testing;

import it.nexoapp.common.tenant.TenantNotResolvedException;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Reusable two-tenant leak test base, published in the test-jar of the common library.
 * <p>
 * A service proves the tenant isolation of one of its tenant tables by subclassing this class (annotated with
 * {@code @QuarkusTest}) and implementing the four hooks. The four inherited tests then run against the real
 * database: a foreign row is invisible, uniqueness is per tenant, an empty tenant sees nothing and a session
 * without a tenant fails closed.
 * <p>
 * How to implement the hooks:
 * <ul>
 *   <li>{@link #createAs(UUID, String)}, {@link #existsAs(UUID, UUID)} and {@link #countAs(UUID)} open a scope with
 *   {@code TenantContext.callAs(tenant, ...)} and, INSIDE that scope, a transaction with
 *   {@code QuarkusTransaction.requiringNew()}; the scope is always set before the transaction starts and one
 *   transaction is used per scope;</li>
 *   <li>{@link #countOnCurrentThread()} opens a session WITHOUT any tenant scope (a plain
 *   {@code QuarkusTransaction.requiringNew()} call) and must not be wrapped in {@code TenantContext}.</li>
 * </ul>
 * The unique key passed to {@link #createAs(UUID, String)} must map to the natural unique key of the table under
 * test (for example a tax code or an email) so that the per-tenant uniqueness test is meaningful. Keys are random
 * per test, so repeated runs on a shared Dev Services database stay independent.
 */
public abstract class TenantIsolationHarness {

    /* - tenants - */
    /** First fixed tenant of the harness. */
    protected static final UUID TENANT_A = UUID.fromString("11111111-1111-4111-8111-111111111111");

    /** Second fixed tenant of the harness. */
    protected static final UUID TENANT_B = UUID.fromString("22222222-2222-4222-8222-222222222222");

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a row of the table under test in its own scope and transaction.
     *
     * @param tenant    tenant that owns the row
     * @param uniqueKey value for the natural unique key of the table, unique within one tenant
     * @return the identifier of the created row
     */
    protected abstract UUID createAs(UUID tenant, String uniqueKey);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tells whether a row is visible to a tenant.
     *
     * @param tenant tenant that looks for the row
     * @param id     identifier of the row
     * @return true when the row is found under that tenant
     */
    protected abstract boolean existsAs(UUID tenant, UUID id);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Counts the rows visible to a tenant.
     *
     * @param tenant tenant that counts
     * @return the number of visible rows
     */
    protected abstract long countAs(UUID tenant);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Counts the rows on the calling thread through a session opened WITHOUT any tenant scope.
     *
     * @return the number of rows (never reached when the session fails closed)
     */
    protected abstract long countOnCurrentThread();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A row created under one tenant is never visible to another tenant.
     */
    @Test
    void foreignRowIsNeverVisible() {
        final UUID id = createAs(TENANT_A, uniqueKey());

        assertFalse(existsAs(TENANT_B, id), "the row of tenant A must be invisible to tenant B");
        assertTrue(existsAs(TENANT_A, id), "the row must be visible to its owning tenant");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The unique key is unique per tenant: the same key is accepted under two tenants and refused twice under one.
     */
    @Test
    void uniquenessIsPerTenant() {
        final String key = uniqueKey();

        final UUID idA = createAs(TENANT_A, key);
        final UUID idB = createAs(TENANT_B, key);
        assertNotEquals(idA, idB, "the same key under two tenants gives two rows");

        assertThrows(Exception.class, () -> createAs(TENANT_A, key), "the same key twice under one tenant");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A tenant without any row sees nothing, whatever the other tenants hold.
     */
    @Test
    void emptyTenantSeesNothing() {
        createAs(TENANT_A, uniqueKey());
        createAs(TENANT_B, uniqueKey());

        assertEquals(0L, countAs(UUID.randomUUID()), "a fresh tenant must see no rows");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A session opened on a bare thread without a tenant scope fails closed, there is no default tenant. The refusal
     * is either the resolver's {@link TenantNotResolvedException} or Hibernate's own "no tenant identifier" error
     * (Quarkus only consults the resolver while a request context is active, bare threads are refused by Hibernate).
     *
     * @throws InterruptedException when the executor is interrupted
     */
    @Test
    void sessionWithoutTenantFailsClosed() throws InterruptedException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(this::countOnCurrentThread).get();
            fail("a session without a tenant must fail closed");
        } catch (final ExecutionException e) {
            assertTrue(isTenantRefusal(e), "tenant refusal expected in the cause chain but was: " + e);
        } finally {
            executor.shutdownNow();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds a random unique key.
     *
     * @return a key of 16 characters
     */
    private static String uniqueKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Checks whether the cause chain holds a tenant refusal.
     *
     * @param throwable throwable to inspect
     * @return true when a {@link TenantNotResolvedException} or a Hibernate "no tenant identifier" error is found
     */
    private static boolean isTenantRefusal(final Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof TenantNotResolvedException) {
                return true;
            }
            if (t instanceof HibernateException && t.getMessage() != null
                    && t.getMessage().contains("no tenant identifier")) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }

}
