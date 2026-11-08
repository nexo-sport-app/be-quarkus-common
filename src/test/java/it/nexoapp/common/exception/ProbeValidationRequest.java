package it.nexoapp.common.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request of the validation probe: three fields, one of them with two constraints that can fail together.
 *
 * @param name   display name, 2 to 40 characters and not blank
 * @param taxCode tax code, exactly eleven digits
 * @param age    age, mandatory
 */
public record ProbeValidationRequest(
        @NotBlank @Size(min = 2, max = 40) String name,
        @Pattern(regexp = "^[0-9]{11}$") String taxCode,
        @NotNull Integer age) {
}
