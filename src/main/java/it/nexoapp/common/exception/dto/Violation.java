package it.nexoapp.common.exception.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

/**
 * One violated validation rule of a request. It names the rule and the field, never the rejected value.
 *
 * @param field   dotted path of the violated property relative to the request body, {@code body} when the rule
 *                concerns the body as a whole
 * @param code    simple name of the violated constraint, for example {@code NotBlank}
 * @param message interpolated message of the constraint
 */
@Schema(description = "One violated validation rule; it never carries the rejected value")
public record Violation(
        @Schema(description = "Path of the violated property, 'body' when the rule concerns the whole body", examples = "taxCode")
        String field,
        @Schema(description = "Simple name of the violated constraint", examples = "Pattern")
        String code,
        @Schema(description = "Message of the violated constraint", examples = "must match \"^[0-9]{11}$\"")
        String message) implements Serializable {
}
