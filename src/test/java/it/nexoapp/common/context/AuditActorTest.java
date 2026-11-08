package it.nexoapp.common.context;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import it.nexoapp.common.tenant.probe.ProbeEntity;
import it.nexoapp.common.tenant.probe.ProbeRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Runs a task on a bare thread and returns its result.
     *
     * @param <T>  type of the result
     * @param task task to run
     * @return the task result
     * @throws Exception when the task fails
     */
    private <T> T onBareThread(final Callable<T> task) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return executor.submit(task).get();
        } finally {
            executor.shutdownNow();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Updates the last name of a row in its own transaction, on the calling thread.
     *
     * @param id identifier of the row
     */
    private void touch(final UUID id) {
        QuarkusTransaction.requiringNew().run(() -> repository.findById(id).setLastName("Bianchi"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An explicit idp actor is used inside the scope, the system actor applies again to a later update.
     *
     * @throws Exception when the executor fails
     */
    @Test
    void explicitIdpScopeAttributesCreateAndLaterUpdateFallsBackToSystem() throws Exception {
        final UUID id = onBareThread(() -> ExecutionScope.callAs("idp:sub-x", this::persistProbe));
        assertEquals("idp:sub-x", createdBy(id), "created_by inside the scope");
        assertEquals("idp:sub-x", updatedBy(id), "updated_by inside the scope");

        onBareThread(() -> {
            touch(id);
            return null;
        });
        assertEquals("idp:sub-x", createdBy(id), "created_by is never rewritten");
        assertEquals("system:cmn", updatedBy(id), "updated_by after the scope ended");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A member actor is the canonical lowercase UUID string.
     *
     * @throws Exception when the executor fails
     */
    @Test
    void memberActorIsLowercaseCanonicalUuid() throws Exception {
        final UUID member = UUID.fromString("ABCDEF01-2345-4789-ABCD-EF0123456789");
        final String actor = ExecutionScope.memberActor(member);
        assertEquals("abcdef01-2345-4789-abcd-ef0123456789", actor, "member actor encoding");

        final UUID id = onBareThread(() -> ExecutionScope.callAs(actor, this::persistProbe));
        assertEquals("abcdef01-2345-4789-abcd-ef0123456789", createdBy(id), "created_by of a member write");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The actor prefixes are exactly {@code system:} and {@code idp:}.
     */
    @Test
    void actorFactoriesUseTheFixedPrefixes() {
        assertEquals("system:job-a", ExecutionScope.systemActor("job-a"), "system prefix");
        assertEquals("idp:sub-1", ExecutionScope.idpActor("sub-1"), "idp prefix");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Inner scopes win while active and the outer actor is restored when they end.
     *
     * @throws Exception when the executor fails
     */
    @Test
    void nestedScopesRestoreTheOuterActor() throws Exception {
        final UUID[] ids = new UUID[3];
        onBareThread(() -> {
            ExecutionScope.runAs("idp:sub-y", () -> {
                ids[0] = persistProbe();
                ExecutionScope.runAs("system:job-a", () -> ids[1] = persistProbe());
                ids[2] = persistProbe();
            });
            assertTrue(ExecutionScope.currentActor().isEmpty(), "no actor left after the outer scope");
            return null;
        });

        assertEquals("idp:sub-y", createdBy(ids[0]), "before the inner scope");
        assertEquals("system:job-a", createdBy(ids[1]), "inside the inner scope");
        assertEquals("idp:sub-y", createdBy(ids[2]), "after the inner scope");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The previous actor is restored even when the scope body throws.
     */
    @Test
    void scopeIsRestoredWhenTheBodyThrows() {
        ExecutionScope.runAs("idp:outer", () -> {
            assertThrows(IllegalStateException.class, () -> ExecutionScope.runAs("idp:inner", () -> {
                throw new IllegalStateException("boom");
            }));
            assertEquals("idp:outer", ExecutionScope.currentActor().orElseThrow(), "outer actor restored");
        });
        assertTrue(ExecutionScope.currentActor().isEmpty(), "no actor left on the test thread");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Two threads running different scopes at the same time never see each other's actor.
     *
     * @throws Exception when the executor fails
     */
    @Test
    void parallelScopesStayThreadConfined() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch bothInside = new CountDownLatch(2);
        try {
            final Future<UUID> first = executor.submit(() -> ExecutionScope.callAs("idp:thread-a", () -> {
                await(bothInside);
                return persistProbe();
            }));
            final Future<UUID> second = executor.submit(() -> ExecutionScope.callAs("idp:thread-b", () -> {
                await(bothInside);
                return persistProbe();
            }));

            assertEquals("idp:thread-a", createdBy(first.get(30, TimeUnit.SECONDS)), "first thread actor");
            assertEquals("idp:thread-b", createdBy(second.get(30, TimeUnit.SECONDS)), "second thread actor");
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
     * A blank or null actor is rejected and the task does not run.
     */
    @Test
    void blankOrNullActorIsRejectedAndNothingRuns() {
        final AtomicBoolean ran = new AtomicBoolean(false);

        assertThrows(IllegalArgumentException.class, () -> ExecutionScope.runAs(null, () -> ran.set(true)));
        assertThrows(IllegalArgumentException.class, () -> ExecutionScope.runAs("", () -> ran.set(true)));
        assertThrows(IllegalArgumentException.class, () -> ExecutionScope.runAs("   ", () -> ran.set(true)));
        assertThrows(IllegalArgumentException.class, () -> ExecutionScope.callAs(" ", () -> ran.getAndSet(true)));
        assertThrows(IllegalArgumentException.class, () -> ExecutionScope.systemActor(" "));
        assertThrows(IllegalArgumentException.class, () -> ExecutionScope.idpActor(null));
        assertThrows(IllegalArgumentException.class, () -> ExecutionScope.memberActor(null));

        assertFalse(ran.get(), "the task must not run with an invalid actor");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The test thread has an active request context but no request branch exists yet: it gets the system actor.
     */
    @Test
    void requestContextThreadWithoutScopeGetsSystemActor() {
        final UUID id = persistProbe();

        assertEquals("system:cmn", createdBy(id), "created_by on the test thread");
        assertEquals("system:cmn", updatedBy(id), "updated_by on the test thread");
    }

}
