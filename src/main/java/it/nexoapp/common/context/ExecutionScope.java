package it.nexoapp.common.context;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Thread-confined scope that fixes the audit actor for the code running inside it.
 * <p>
 * Jobs, message consumers and the bootstrap run outside an HTTP request, so nobody can derive
 * the actor from a token. They declare it explicitly with {@link #runAs(String, Runnable)} or
 * {@link #callAs(String, Supplier)}. The actor is stored in a {@link ThreadLocal}: it is never
 * visible to other threads, scopes nest, and the previous value is always restored when a scope
 * ends, also when the body throws.
 * <p>
 * Actor strings use a fixed vocabulary: {@code system:<app.code>} for the application itself,
 * {@code idp:<subject>} for an identity not yet linked to a member and the canonical lowercase
 * UUID string for a member. Use the factories {@link #systemActor(String)},
 * {@link #idpActor(String)} and {@link #memberActor(UUID)} to build them.
 */
public final class ExecutionScope {

    /* - state - */
    /** Actor of the scope active on the current thread, absent when no scope is active. */
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Not instantiable: static utility.
     */
    private ExecutionScope() {
        throw new IllegalStateException("Utility class");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Runs a task with the given actor as the audit actor of the current thread.
     *
     * @param actor audit actor, never null or blank
     * @param task  task to run
     * @throws IllegalArgumentException if the actor is null or blank, in which case nothing runs
     */
    public static void runAs(final String actor, final Runnable task) {
        callAs(actor, () -> {
            task.run();
            return null;
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Calls a supplier with the given actor as the audit actor of the current thread.
     *
     * @param <T>   type of the supplied value
     * @param actor audit actor, never null or blank
     * @param task  supplier to call
     * @return the value supplied by the task
     * @throws IllegalArgumentException if the actor is null or blank, in which case nothing runs
     */
    public static <T> T callAs(final String actor, final Supplier<T> task) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("The audit actor must not be null or blank");
        }
        final String previous = CURRENT.get();
        CURRENT.set(actor);
        try {
            return task.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Actor of the scope active on the current thread.
     *
     * @return the explicit actor, empty when no scope is active
     */
    public static Optional<String> currentActor() {
        return Optional.ofNullable(CURRENT.get());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds the actor of the application itself.
     *
     * @param appCode short code of the microservice ({@code app.code})
     * @return {@code system:<appCode>}
     * @throws IllegalArgumentException if the code is null or blank
     */
    public static String systemActor(final String appCode) {
        return "system:" + requireText(appCode, "app code");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds the actor of an identity not (yet) linked to a member.
     *
     * @param subject subject ({@code sub}) of the identity provider
     * @return {@code idp:<subject>}
     * @throws IllegalArgumentException if the subject is null or blank
     */
    public static String idpActor(final String subject) {
        return "idp:" + requireText(subject, "subject");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds the actor of a member: the canonical lowercase UUID string.
     *
     * @param memberId identifier of the member
     * @return the lowercase canonical string form of the identifier
     * @throws IllegalArgumentException if the identifier is null
     */
    public static String memberActor(final UUID memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("The member id must not be null");
        }
        return memberId.toString().toLowerCase(Locale.ROOT);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Rejects null or blank text.
     *
     * @param value text to check
     * @param label name of the value, used in the error message
     * @return the value unchanged
     */
    private static String requireText(final String value, final String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The " + label + " must not be null or blank");
        }
        return value;
    }

}
