package it.nexoapp.common.context;

import it.nexoapp.common.constant.Language;
import jakarta.enterprise.context.RequestScoped;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the context of an HTTP request during its lifecycle.
 * <p>
 * This class holds metadata and the resolved identifiers of the caller for the current request:
 * headers, call id, client address, locale, the identity provider subject and the association and
 * member the request acts as. It provides a centralized way to access and manage request-specific
 * data in a thread-safe and scope-limited manner.
 * </p>
 * <p>
 * Personal data (names, usernames, emails, tax codes) never goes here: it would end up in logs and
 * audit trails. Only opaque identifiers are held.
 * </p>
 * <p>
 * The {@code RequestScoped} annotation indicates that the lifecycle of this
 * class is bound to the scope of an HTTP request.
 */
@Data
@RequestScoped
public class RequestContext implements Serializable {

    @Serial
    private static final long serialVersionUID = -7978563029792028589L;

    // --
    // Request metadata
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Represents the HTTP headers associated with the current request.
     * <p>
     * This field contains a map of header names to their corresponding values.
     * Since HTTP headers can have multiple values for the same header name,
     * each entry maps a header name (String) to a list of values (List of Strings).
     * This is used for accessing request metadata such as content type, authorization
     * tokens, custom headers, and other HTTP protocol information.
     * <p>
     * Example: {@code {"Content-Type": ["application/json"], "Authorization": ["Bearer token123"]}}
     */
    private Map<String, List<String>> headers;

    /**
     * Represents a unique identifier for tracking a specific API call or request.
     * <p>
     * This field is used for logging, debugging, and tracing requests across
     * distributed systems. It helps correlate log entries and monitor the flow
     * of a single request through various services and components.
     * <p>
     * Example: {@code "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"}
     */
    private String callId;

    /**
     * Represents the IP address of the client making the request.
     * <p>
     * This field captures the originating IP address, which can be used for
     * security auditing, geolocation, rate limiting, and access control.
     * It may represent either an IPv4 or IPv6 address.
     * <p>
     * Example: {@code "192.168.1.100"} or {@code "2001:0db8:85a3:0000:0000:8a2e:0370:7334"}
     */
    private String ip;

    /**
     * Represents the preferred language for the current request.
     * <p>
     * This field indicates the language setting for localization and
     * internationalization purposes. It is used to determine which language
     * version of content, messages, and resources should be provided to the user.
     * <p>
     * Example: {@code Language.EN}, {@code Language.IT}
     */
    private Language language;

    // --
    // Caller identifiers
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Represents the subject ({@code sub}) of the caller at the identity provider.
     * <p>
     * It is an opaque identifier, not personal data. It is set for every authenticated request whose
     * identity carries an issuer and a subject, linked to a member or not.
     * <p>
     * Example: {@code "7c9e6679-7425-40de-944b-e07fc1f90ae7"}
     */
    private String subject;

    /**
     * Represents the association (tenant) the request acts in.
     * <p>
     * It is set only when the caller is linked to exactly one selected membership; it is {@code null}
     * for unlinked and ambiguous callers, so such a request has no tenant.
     * <p>
     * Example: {@code 3f2504e0-4f89-11d3-9a0c-0305e82c3301}
     */
    private UUID associationId;

    /**
     * Represents the member the request acts as, inside {@link #associationId}.
     * <p>
     * It is set only when the caller is linked, and it is the audit actor of the request's writes.
     * <p>
     * Example: {@code a1a1a1a1-a1a1-41a1-81a1-a1a1a1a1a1a1}
     */
    private UUID memberId;

}
