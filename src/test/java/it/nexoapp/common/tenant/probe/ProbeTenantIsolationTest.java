package it.nexoapp.common.tenant.probe;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.nexoapp.common.tenant.TenantContext;
import it.nexoapp.common.testing.TenantIsolationHarness;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;

import java.util.UUID;

/**
 * Proves the reusable {@link TenantIsolationHarness} against the probe table: the unique key is the tax code.
 */
@QuarkusTest
class ProbeTenantIsolationTest extends TenantIsolationHarness {

    /* - repository - */
    @Inject
    ProbeRepository repository;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Removes the rows of both harness tenants after each test.
     */
    @AfterEach
    void cleanUp() {
        for (final UUID tenant : new UUID[]{TENANT_A, TENANT_B}) {
            TenantContext.runAs(tenant, () -> QuarkusTransaction.requiringNew().run(() -> repository.deleteAll()));
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected UUID createAs(final UUID tenant, final String uniqueKey) {
        return TenantContext.callAs(tenant, () -> QuarkusTransaction.requiringNew().call(() -> {
            final ProbeEntity probe = ProbeEntity.builder().taxCode(uniqueKey).lastName("Rossi").build();
            repository.persist(probe);
            return probe.getId();
        }));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean existsAs(final UUID tenant, final UUID id) {
        return TenantContext.callAs(tenant, () -> QuarkusTransaction.requiringNew().call(() ->
                repository.findById(id) != null));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected long countAs(final UUID tenant) {
        return TenantContext.callAs(tenant, () -> QuarkusTransaction.requiringNew().call(() -> repository.count()));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected long countOnCurrentThread() {
        return QuarkusTransaction.requiringNew().call(() -> repository.count());
    }

}
