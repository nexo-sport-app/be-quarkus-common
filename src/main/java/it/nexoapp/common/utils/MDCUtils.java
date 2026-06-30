package it.nexoapp.common.utils;

import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.exception.IllegalInstantiationException;
import org.slf4j.MDC;

/**
 * Utility class for interacting with the Mapped Diagnostic Context (MDC).
 */
public class MDCUtils {

    private MDCUtils() {
        throw new IllegalInstantiationException();
    }

    /**
     * Retrieves the value associated with the specified {@link MDCConstant} key
     * from the MDC.
     *
     * @param key the {@link MDCConstant} key for which the value is to be retrieved
     * @return the value associated with the specified key in the MDC, or {@code null}
     *         if the key does not exist in the context
     */
    public static String get(final MDCConstant key) {
        return MDC.get(key.name());
    }

    /**
     * Associates the specified value with the specified key in the MDC.
     * This allows diagnostic and contextual information to be included in log messages for better
     * debugging and tracing.
     *
     * @param key   the {@link MDCConstant} key with which the value is to be associated
     * @param value the value to be associated with the specified key in the MDC
     */
    public static void put(final MDCConstant key, final String value) {
        MDC.put(key.name(), value);
    }

    /**
     * Removes the value associated with the specified {@link MDCConstant} key
     * from the MDC. This operation is used to clear
     * contextual information that is no longer needed in the logging context.
     *
     * @param key the {@link MDCConstant} key whose associated value is to be removed
     */
    public static void remove(final MDCConstant key) {
        MDC.remove(key.name());
    }

    /**
     * Clears all entries in the Mapped Diagnostic Context (MDC).
     * This method removes all key-value pairs associated with the current thread's
     * logging context, effectively resetting it.
     */
    public static void clear() {
        MDC.clear();
    }

}
