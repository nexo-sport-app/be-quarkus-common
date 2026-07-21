package it.nexoapp.common.constant;

import it.nexoapp.common.exception.IllegalInstantiationException;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

/**
 * The Headers class provides a set of constant string values representing
 * header keys used in HTTP requests. These header keys are typically used
 * to convey metadata.
 * <br/>
 * This class is designed to be used as a collection of predefined header keys 
 * for consistent usage across different components of an application, ensuring 
 * standardized header naming conventions.
 */
public final class Headers {

    private Headers() {
        throw new IllegalInstantiationException();
    }

    /**
     * Header key for the unique identifier of an API call.
     * <p>
     * This header contains a unique identifier (typically a UUID) that is assigned to each API request.
     * It is used for request tracing, debugging, and correlating logs across distributed systems.
     * If not provided by the client, the system will automatically generate a UUID for the request.
     * </p>
     * <p>
     * <b>Example:</b> {@code x-call-id: 550e8400-e29b-41d4-a716-446655440000}
     * </p>
     *
     * @see java.util.UUID
     */
    @Parameter(description = "Unique identifier for the API call", example = "550e8400-e29b-41d4-a716-446655440000")
    public static final String CALL_ID = "x-call-id";

    /**
     * Header key for the identifier of the calling application or service.
     * <p>
     * This header specifies the name or identifier of the client application or service
     * that is making the API request. It helps in identifying the source of the request
     * for monitoring, analytics, and access control purposes.
     * </p>
     * <p>
     * <b>Example:</b> {@code x-caller: WEB}
     * </p>
     * <p>
     * <b>Common values:</b>
     * <ul>
     *   <li>{@code WEB} - Web application</li>
     *   <li>{@code MOBILE} - Mobile application</li>
     *   <li>{@code INTERNAL} - Backend service</li>
     * </ul>
     * </p>
     */
    @Parameter(description = "Identifier of the calling application or service", example = "WEB")
    public static final String CALLER = "x-caller";

    /**
     * Header key for the version of the calling application.
     * <p>
     * This header contains the version number of the client application making the API request.
     * It is useful for tracking which application versions are in use, supporting backward
     * compatibility, and troubleshooting version-specific issues.
     * </p>
     * <p>
     * <b>Example:</b> {@code x-app-version: 1.0.0}
     * </p>
     * <p>
     * <b>Version format:</b> Typically follows semantic versioning (MAJOR.MINOR.PATCH)
     * <ul>
     *   <li>{@code 1.0.0} - Initial release</li>
     *   <li>{@code 1.2.3} - Version 1, feature update 2, patch 3</li>
     *   <li>{@code 2.0.0} - Major version with breaking changes</li>
     * </ul>
     * </p>
     */
    @Parameter(description = "Version of the calling application", example = "1.0.0")
    public static final String APP_VERSION = "x-app-version";

}