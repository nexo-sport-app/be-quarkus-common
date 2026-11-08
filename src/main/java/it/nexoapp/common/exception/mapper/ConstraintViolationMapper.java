package it.nexoapp.common.exception.mapper;

import it.nexoapp.common.configuration.AppInfo;
import it.nexoapp.common.constant.LoggerName;
import it.nexoapp.common.exception.dto.Fault;
import it.nexoapp.common.exception.dto.Violation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Returns every Bean Validation violation of a request in a single {@link Fault} (FND-05), as 400.
 * <p>
 * Each violation is reduced to field, constraint name and interpolated message: the rejected value is never
 * part of the answer. Violations are sorted by field and then by code, so the order is stable across calls.
 * A violation raised on a return value is a server defect and answers 500 without details.
 */
@Provider
@RequiredArgsConstructor
class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    /** Fault code of a validation failure. */
    static final String CODE = "nexo.validation";

    /** Generic message of a validation failure. */
    static final String MESSAGE = "Validation failed";

    /** Field name used when the violated rule concerns the request body as a whole. */
    static final String BODY = "body";

    /* - logger - */
    /** Logger; it never receives rejected values. */
    private static final Logger logger = LoggerFactory.getLogger(LoggerName.EXCEPTION);

    /* - application - */
    /** Application info, the origin of the Fault when the MDC holds none. */
    private final AppInfo appInfo;

    /* - jax-rs - */
    /** Headers of the current request, the source of the call id when the MDC holds none. */
    @Context
    HttpHeaders headers;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final ConstraintViolationException exception) {
        final Set<ConstraintViolation<?>> raw = exception.getConstraintViolations();
        if (isReturnValue(raw)) {
            final Fault fault = FaultFactory.fault(appInfo, headers, "nexo.exception.unexpected", "Unexpected error");
            logger.error("A response failed its own validation, callId={}", fault.getTrace(), exception);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
                    .entity(fault).build();
        }
        final List<Violation> violations = raw.stream()
                .map(v -> new Violation(field(v.getPropertyPath()), code(v), v.getMessage()))
                .sorted(Comparator.comparing(Violation::field).thenComparing(Violation::code)
                        .thenComparing(Violation::message))
                .toList();
        final Fault fault = FaultFactory.fault(appInfo, headers, CODE, MESSAGE, violations);
        logger.warn("Client error 400 code={} message={} callId={}", CODE, MESSAGE, fault.getTrace());
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(fault).build();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Tells whether any violation was raised on a return value rather than on the request.
     *
     * @param violations the violations of the exception
     * @return true when at least one violation concerns a return value
     */
    private static boolean isReturnValue(final Set<ConstraintViolation<?>> violations) {
        return violations.stream().anyMatch(v -> {
            for (final Path.Node node : v.getPropertyPath()) {
                if (node.getKind() == ElementKind.RETURN_VALUE) {
                    return true;
                }
            }
            return false;
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds the field of a violation: the property path without its method and parameter nodes, dotted for nested
     * properties and with the index for collection elements; {@value #BODY} when nothing remains.
     *
     * @param path the property path of the violation
     * @return the field name
     */
    private static String field(final Path path) {
        final StringBuilder field = new StringBuilder();
        for (final Path.Node node : path) {
            switch (node.getKind()) {
                case METHOD, CONSTRUCTOR, PARAMETER, CROSS_PARAMETER, RETURN_VALUE -> {
                    // not part of the request shape
                }
                default -> {
                    if (node.isInIterable() && !field.isEmpty()) {
                        field.append('[').append(node.getIndex() == null ? "" : node.getIndex()).append(']');
                    }
                    if (node.getKind() != ElementKind.CONTAINER_ELEMENT && node.getName() != null) {
                        if (!field.isEmpty()) {
                            field.append('.');
                        }
                        field.append(node.getName());
                    }
                }
            }
        }
        return field.isEmpty() ? BODY : field.toString();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Names the violated constraint by the simple name of its annotation.
     *
     * @param violation the violation
     * @return the simple name, for example {@code NotBlank}
     */
    private static String code(final ConstraintViolation<?> violation) {
        return violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
    }

}
