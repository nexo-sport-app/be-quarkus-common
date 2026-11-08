package it.nexoapp.common.context;

/**
 * Resolves who is performing the current write, independently of any HTTP request.
 * <p>
 * Used by the audit listener to fill {@code created_by} and {@code updated_by}. The result is
 * never null or blank, so audit columns are always attributed.
 */
public interface ActorContext {

    /**
     * Actor of the current thread: the explicit {@link ExecutionScope} actor when one is active,
     * otherwise the system actor of the running application.
     *
     * @return the actor string, never null or blank
     */
    String currentActor();

}
