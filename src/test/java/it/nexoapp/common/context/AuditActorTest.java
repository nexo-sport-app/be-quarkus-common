package it.nexoapp.common.context;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.nexoapp.common.tenant.probe.ProbeEntity;
import it.nexoapp.common.tenant.probe.ProbeRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Audit actor matrix on a real Postgres: bare threads, explicit scopes, nested and parallel scopes.
 * Bare executor threads have no request context, which is exactly where jobs and consumers write.
 */
@QuarkusTest
class AuditActorTest {

    /* - repository - */
    @Inject
    ProbeRepository repository;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Removes the rows created by a test.
     */
    @AfterEach
    void cleanUp() {
        QuarkusTransaction.requiringNew().run(() -> repository.deleteAll());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Persists a probe row in its own transaction and returns its identifier.
     *
     * @return the identifier of the new row
     */
    private UUID persistProbe() {
        return QuarkusTransaction.requiringNew().call(() -> {
            final ProbeEntity probe = ProbeEntity.builder()
                    .taxCode(UUID.randomUUID().toString().substring(0, 16))
                    .lastName("Rossi")
                    .build();
            repository.persist(probe);
            return probe.getId();
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Reads the stored created_by of a row.
     *
     * @param id identifier of the row
     * @return the created_by value
     */
    private String createdBy(final UUID id) {
        return QuarkusTransaction.requiringNew().call(() -> repository.findById(id).getCreatedBy());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Reads the stored updated_by of a row.
     *
     * @param id identifier of the row
     * @return the updated_by value
     */
    private String updatedBy(final UUID id) {
        return QuarkusTransaction.requiringNew().call(() -> repository.findById(id).getUpdatedBy());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A write from a bare thread (no request, no scope) is attributed to the system actor.
     *
     * @throws Exception when the executor fails
     */
    @Test
    void bareThreadWriteIsAttributedToSystemActor() throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final UUID id = executor.submit(this::persistProbe).get();

            assertEquals("system:cmn", createdBy(id), "created_by of a bare-thread write");
            assertEquals("system:cmn", updatedBy(id), "updated_by of a bare-thread write");
        } catch (final ExecutionException e) {
            throw new AssertionError("bare-thread write must not fail", e.getCause());
        } finally {
            executor.shutdownNow();
        }
    }

}
