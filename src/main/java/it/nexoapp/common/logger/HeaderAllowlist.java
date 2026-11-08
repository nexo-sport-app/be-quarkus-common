package it.nexoapp.common.logger;

import it.nexoapp.common.exception.IllegalInstantiationException;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Decides which HTTP headers may reach the logs (D-19).
 * <p>
 * Headers are logged by allowlist only: a header that is not listed in {@link #ALLOWED} is dropped entirely, its
 * name and its value alike, so a new or unexpected header can never leak by accident. The match ignores the letter
 * case of the header name. No credential-carrying header is, or may ever be, on the list.
 */
public final class HeaderAllowlist {

    /* - constants - */
    /** Lower case names of the headers that may be logged; the platform headers match {@code Headers}. */
    public static final Set<String> ALLOWED = Set.of(
            "content-type",
            "content-length",
            "accept",
            "accept-language",
            "user-agent",
            "x-call-id",
            "x-caller",
            "x-app-version",
            "x-association-id");

    /** Matches the characters that could forge a log line or garble it: control characters. */
    private static final String CONTROL_CHARACTERS = "[\\x00-\\x1F\\x7F]";

    // -----------------------------------------------------------------------------------------------------------------

    private HeaderAllowlist() {
        throw new IllegalInstantiationException();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Keeps only the allowlisted headers.
     * <p>
     * Names are lower-cased, values are written with {@link String#valueOf(Object)} and have their control
     * characters replaced, because header values are client-controlled.
     *
     * @param headers the headers of a request or a response, may be null
     * @return the allowlisted headers by lower case name, in the input order, never null
     */
    public static Map<String, List<String>> filter(final MultivaluedMap<String, ?> headers) {
        final Map<String, List<String>> kept = new LinkedHashMap<>();
        if (headers == null) {
            return kept;
        }
        for (final Map.Entry<String, ? extends List<?>> entry : headers.entrySet()) {
            final String name = entry.getKey() == null ? "" : entry.getKey().toLowerCase(Locale.ROOT);
            if (ALLOWED.contains(name) && entry.getValue() != null) {
                kept.computeIfAbsent(name, key -> new ArrayList<>())
                        .addAll(entry.getValue().stream().map(value -> sanitize(String.valueOf(value))).toList());
            }
        }
        return kept;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Replaces the control characters of a value.
     *
     * @param value the raw value
     * @return the value with every control character replaced by an underscore
     */
    private static String sanitize(final String value) {
        return value.replaceAll(CONTROL_CHARACTERS, "_");
    }

}
