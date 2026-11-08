package it.nexoapp.common.exception.mapper;

import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.exception.IllegalInstantiationException;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;

/**
 * Logs the exceptions the mappers turn into Faults: a client error (4xx) is one WARN line without stack trace,
 * everything else is an ERROR with the stack trace.
 */
final class FaultLogger {

    private FaultLogger() {
        throw new IllegalInstantiationException();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Logs a mapped exception according to the family of its status.
     *
     * @param logger    the logger of the mapper
     * @param status    the HTTP status of the Fault
     * @param code      the fault code
     * @param message   the fault message
     * @param exception the mapped exception, logged with its stack trace only for non-client errors
     */
    static void log(final Logger logger, final Response.Status status, final String code, final String message,
                    final Throwable exception) {
        if (status.getFamily() == Response.Status.Family.CLIENT_ERROR) {
            logger.warn("Client error {} code={} message={} callId={}", status.getStatusCode(), code, message,
                    MDCUtils.get(MDCConstant.CALL_ID));
        } else {
            logger.error("Caught an exception, status={} code={} callId={}", status.getStatusCode(), code,
                    MDCUtils.get(MDCConstant.CALL_ID), exception);
        }
    }

}
