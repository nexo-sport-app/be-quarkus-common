package it.nexoapp.common.exception.mapper;

import it.nexoapp.common.configuration.AppInfo;
import it.nexoapp.common.constant.Headers;
import it.nexoapp.common.constant.MDCConstant;
import it.nexoapp.common.domain.DateTime;
import it.nexoapp.common.exception.IllegalInstantiationException;
import it.nexoapp.common.exception.dto.Fault;
import it.nexoapp.common.exception.dto.Violation;
import it.nexoapp.common.utils.MDCUtils;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Builds the {@link Fault} of the mappers that can run before the request context filter, so the trace and the
 * origin cannot always be read from the MDC.
 */
final class FaultFactory {

    private FaultFactory() {
        throw new IllegalInstantiationException();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Resolves the trace of a Fault: the MDC call id, else the call id header of the request, else a fresh id.
     *
     * @param headers the headers of the current request, may be null
     * @return the trace, never empty
     */
    static String trace(final HttpHeaders headers) {
        final String fromMdc = MDCUtils.get(MDCConstant.CALL_ID);
        if (StringUtils.isNotEmpty(fromMdc)) {
            return fromMdc;
        }
        final String fromHeader = headers == null ? null : headers.getHeaderString(Headers.CALL_ID);
        return StringUtils.defaultIfEmpty(fromHeader, UUID.randomUUID().toString());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds a Fault stamped now, without violations.
     *
     * @param appInfo application info, the origin when the MDC holds none
     * @param headers the headers of the current request, may be null
     * @param code    the fault code
     * @param message the fault message
     * @return the Fault
     */
    static Fault fault(final AppInfo appInfo, final HttpHeaders headers, final String code, final String message) {
        return fault(appInfo, headers, code, message, null);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds a Fault stamped now.
     *
     * @param appInfo    application info, the origin when the MDC holds none
     * @param headers    the headers of the current request, may be null
     * @param code       the fault code
     * @param message    the fault message
     * @param violations the violated rules, null when the Fault is not a validation Fault
     * @return the Fault
     */
    static Fault fault(final AppInfo appInfo, final HttpHeaders headers, final String code, final String message,
                       final List<Violation> violations) {
        return Fault.builder()
                .trace(trace(headers))
                .origin(StringUtils.defaultIfEmpty(MDCUtils.get(MDCConstant.APP_CODE), appInfo.code()))
                .timestamp(DateTime.now())
                .code(code)
                .message(message)
                .violations(violations)
                .build();
    }

}
