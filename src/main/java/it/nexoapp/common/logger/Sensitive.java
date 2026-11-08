package it.nexoapp.common.logger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a DTO member whose value must never reach the logs.
 * <p>
 * When the {@code it.nexoapp} loggers run at DEBUG, JSON request and response bodies are logged as typed records
 * and every member carrying this annotation is rendered as {@code ##}, also inside nested records and lists (D-18).
 * The annotation has no effect on the JSON sent to or received from the client.
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * public record PersonRequest(String lastName, @Sensitive String taxCode) {
 * }
 * }</pre>
 * is logged as {@code {"lastName":"Rossi","taxCode":"##"}}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Sensitive {
}
