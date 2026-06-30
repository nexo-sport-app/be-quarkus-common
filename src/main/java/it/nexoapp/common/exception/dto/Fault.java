package it.nexoapp.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.nexoapp.common.domain.DateTime;
import lombok.Builder;
import lombok.Value;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * Fault response bean
 */
@Value
@Builder
@Schema(description = "Standardize fault response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fault implements Serializable {

    @Serial
    private static final long serialVersionUID = 6393086023628929915L;

    /**
     * The request unique identifier
     */
    @Schema(description = "Request unique identifier", examples = "6e230a77-6e5d-4784-9818-b086cc686e21")
    String trace;

    /**
     * The origin of the fault
     */
    @Schema(description = "The origin of the fault", examples = "google")
    String origin;

    /**
     * Timestamp of the fault
     */
    @Schema(description = "Timestamp of the exception", examples = "2011-12-03T10:15:30+01:00")
    DateTime timestamp;

    /**
     * Unique fault code to identify the specific error
     */
    @Schema(description = "Unique fault code to identify the specific error", examples = "google.auth.001")
    String code;

    /**
     * Concise technical message about the fault code
     */
    @Schema(description = "Concise technical message about the fault code", examples = "The weather is not good :(")
    String message;

}
