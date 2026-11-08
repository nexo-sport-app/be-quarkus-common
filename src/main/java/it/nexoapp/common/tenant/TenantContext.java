package it.nexoapp.common.tenant;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Thread-confined scope that fixes the tenant (association) for the code running inside it.
 * <p>
 * Jobs, message consumers and the outbox relay run outside an HTTP request, so nobody can derive
 * the tenant from a token. They declare it explicitly with {@link #runAs(UUID, Runnable)} or
 * {@link #callAs(UUID, Supplier)}. Code that must read or write data that belongs to no tenant
 * (the identity directory, the outbox) uses {@link #runUnscoped(Runnable)} or
 * {@link #callUnscoped(Supplier)}: inside it tenant rows are invisible and tenant rows cannot be written.
 * <p>
 * The tenant is stored in a {@link ThreadLocal}: it is never visible to other threads, scopes nest,
 * and the previous value is always restored when a scope ends, also when the body throws.
 * Open the scope before the transaction starts, because Hibernate resolves the tenant when the session opens.
 * <p>
 * Quarkus consults the tenant resolver only while the CDI request context is active (it always is during an
 * HTTP request). On a bare thread (job, consumer, executor) a scope therefore activates the request context
 * for its duration and terminates it afterwards; without a scope and without a request context Hibernate
 * refuses to open the session, so such code still fails closed.
 */
public final class TenantContext {

    /** Sentinel tenant of the unscoped escape hatch: the nil UUID, owned by no association. */
    public static final String NO_TENANT = "00000000-0000-0000-0000-000000000000";

    /* - state - */
    /** Tenant of the scope active on the current thread, absent when no scope is active. */
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Not instantiable: static utility.
     */
    private TenantContext() {
        throw new IllegalStateException("Utility class");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Runs a task with the given tenant as the tenant of the current thread.
     *
     * @param tenantId identifier of the association, never null
     * @param task     task to run
     * @throws IllegalArgumentException if the tenant is null, in which case nothing runs
     */
    public static void runAs(final UUID tenantId, final Runnable task) {
        callAs(tenantId, () -> {
            task.run();
            return null;
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Calls a supplier with the given tenant as the tenant of the current thread.
     *
     * @param <T>      type of the supplied value
     * @param tenantId identifier of the association, never null
     * @param task     supplier to call
     * @return the value supplied by the task
     * @throws IllegalArgumentException if the tenant is null, in which case nothing runs
     */
    public static <T> T callAs(final UUID tenantId, final Supplier<T> task) {
        if (tenantId == null) {
            throw new IllegalArgumentException("The tenant id must not be null");
        }
        return callWith(tenantId.toString(), task);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Runs a task outside every tenant: tenant rows are invisible and cannot be written.
     *
     * @param task task to run
     */
    public static void runUnscoped(final Runnable task) {
        callUnscoped(() -> {
            task.run();
            return null;
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Calls a supplier outside every tenant: tenant rows are invisible and cannot be written.
     *
     * @param <T>  type of the supplied value
     * @param task supplier to call
     * @return the value supplied by the task
     */
    public static <T> T callUnscoped(final Supplier<T> task) {
        return callWith(NO_TENANT, task);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tenant of the scope active on the current thread.
     *
     * @return the explicit tenant (possibly {@link #NO_TENANT}), empty when no scope is active
     */
    public static Optional<String> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tells whether the current thread runs inside an unscoped block.
     *
     * @return true inside {@link #runUnscoped(Runnable)} or {@link #callUnscoped(Supplier)}
     */
    public static boolean isUnscoped() {
        return NO_TENANT.equals(CURRENT.get());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Calls a supplier with the given tenant string and restores the previous value afterwards.
     *
     * @param <T>    type of the supplied value
     * @param tenant tenant string to set
     * @param task   supplier to call
     * @return the value supplied by the task
     */
    private static <T> T callWith(final String tenant, final Supplier<T> task) {
        final ManagedContext requestContext = Arc.container().requestContext();
        final boolean activated = !requestContext.isActive();
        if (activated) {
            requestContext.activate();
        }
        final String previous = CURRENT.get();
        CURRENT.set(tenant);
        try {
            return task.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
            if (activated) {
                requestContext.terminate();
            }
        }
    }

}
