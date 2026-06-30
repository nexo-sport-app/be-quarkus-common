package it.nexoapp.common.domain;

import it.nexoapp.common.constant.Regex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a physical address with structured components.
 * <p>
 * This class provides a standardized way to represent addresses across the application.
 * It includes fields for street addresses, postal codes, city, province, and country information.
 * The class is serializable and can be used in distributed systems or for persistence.
 * </p>
 *
 * <h2>When to Use</h2>
 * <p>Use this class whenever you need to represent a physical address in your domain model, such as:</p>
 * <ul>
 *   <li>User profile addresses (billing, shipping, residential)</li>
 *   <li>Business locations</li>
 *   <li>Store or warehouse addresses</li>
 *   <li>Office locations</li>
 *   <li>Any other physical location requiring structured address data</li>
 * </ul>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Builder pattern support via Lombok for flexible construction</li>
 *   <li>Automatic normalization of whitespace and formatting</li>
 *   <li>Multiple output formats (single-line and multi-line)</li>
 *   <li>Null-safe operations throughout</li>
 *   <li>Country code auto-uppercase on normalization</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Creating an address using the builder
 * Address address = Address.builder()
 *     .line1("123 Main Street")
 *     .line2("Apt 4B")
 *     .postalCode("12345")
 *     .city("New York")
 *     .province("NY")
 *     .country("US")
 *     .build();
 *
 * // Get single-line format
 * String singleLine = address.toString();
 * // Output: "123 Main Street, Apt 4B, 12345 New York, NY, US"
 *
 * // Get multi-line format
 * String multiLine = address.toMultiline();
 * // Output:
 * // 123 Main Street
 * // Apt 4B
 * // 12345 New York
 * // NY
 * // US
 *
 * // Normalize an address (clean whitespace, uppercase country)
 * Address normalized = address.normalized();
 * }</pre>
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Always use the builder pattern for creating new instances</li>
 *   <li>Call {@link #normalized()} after receiving address data from external sources</li>
 *   <li>Use ISO 3166-1 alpha-2 country codes (e.g., "US", "IT", "GB")</li>
 *   <li>Store complete addresses whenever possible; all fields are optional but having more data improves usability</li>
 *   <li>Use {@link #toSingleLine()} for display in constrained spaces (lists, tables)</li>
 *   <li>Use {@link #toMultiline()} for display in detailed views or for printing</li>
 * </ul>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {

    @Serial
    private static final long serialVersionUID = 5897440213118873340L;

    @NotBlank
    @Size(max = 128)
    private String line1;
    @Size(max = 128)
    private String line2;

    @NotBlank
    @Size(max = 8)
    @Pattern(regexp = Regex.POSTAL_CODE, message = "Invalid postal code format")
    private String postalCode;

    @NotBlank
    @Size(max = 64)
    private String city;

    @Size(max = 64)
    private String province;

    @NotBlank
    @Pattern(regexp = Regex.COUNTRY_CODE, message = "Use ISO 3166-1 alpha-2 country code (e.g., IT)")
    private String country;

    // --
    // PUBLIC METHODS
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns a single-line string representation of the address, with components separated by commas.
     * Empty or null components are omitted.
     *
     * @return the formatted address string
     */
    @Override
    public String toString() {
        return normalizeSpaces(
                String.join(", ",
                        nonNullNonBlank(line1),
                        nonNullNonBlank(line2),
                        joinSpace(nonNullNonBlank(postalCode), nonNullNonBlank(city)),
                        nonNullNonBlank(province),
                        nonNullNonBlank(country)
                ).replaceAll("(, )+", ", ").replaceAll("(^, |, $)", "")
        );    }

    /**
     * Returns a single-line representation of the address.
     * Alias for toString() method.
     *
     * @return the formatted address string
     */
    public String toSingleLine() {
        return toString();
    }

    /**
     * Returns a multi-line string representation of the address.
     * Each component is placed on a new line.
     * Empty or null components are omitted.
     *
     * @return the formatted multi-line address string
     */
    public String toMultiline() {
        StringBuilder sb = new StringBuilder();
        if (isNotBlank(line1)) sb.append(line1).append('\n');
        if (isNotBlank(line2)) sb.append(line2).append('\n');
        if (isNotBlank(postalCode) || isNotBlank(city)) {
            sb.append(joinSpace(postalCode, city)).append('\n');
        }
        if (isNotBlank(province)) sb.append(province).append('\n');
        if (isNotBlank(country)) sb.append(country);
        return normalizeSpaces(sb.toString().replaceAll("\\n{2,}", "\n")).strip();
    }

    /**
     * Creates a new normalized Address instance.
     * Normalizes whitespace in all fields and converts country code to uppercase.
     *
     * @return a new Address instance with normalized fields
     */
    public Address normalized() {
        return Address.builder()
                .line1(normalize(line1))
                .line2(normalize(line2))
                .postalCode(normalize(postalCode))
                .city(normalize(city))
                .province(normalize(province))
                .country(normalize(country) != null ? normalize(country).toUpperCase() : null)
                .build();
    }

    // --
    // PRIVATE HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Normalizes a string by trimming and normalizing whitespace.
     * Returns null for empty strings after trimming.
     *
     * @param s the string to normalize
     * @return the normalized string or null
     */
    private static String normalize(final String s) {
        if (s == null) return null;
        String t = s.strip();
        return t.isEmpty() ? null : normalizeSpaces(t);
    }

    /**
     * Replaces multiple whitespace characters with a single space and trims the result.
     *
     * @param s the string to normalize
     * @return the normalized string or null if input is null
     */
    private static String normalizeSpaces(final String s) {
        return s == null ? null : s.replaceAll("\\s+", " ").strip();
    }

    /**
     * Checks if a string is not null and contains at least one non-whitespace character.
     *
     * @param s the string to check
     * @return true if the string is not blank, false otherwise
     */
    private static boolean isNotBlank(final String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Returns the input string if it's not blank, empty string otherwise.
     *
     * @param s the string to check
     * @return the input string or empty string
     */
    private static String nonNullNonBlank(final String s) {
        return isNotBlank(s) ? s : "";
    }

    /**
     * Joins two strings with a space if both are not blank.
     * If one string is blank, returns the other string.
     * If both are blank, returns an empty string.
     *
     * @param a first string
     * @param b second string
     * @return the joined string
     */
    private static String joinSpace(String a, String b) {
        if (isNotBlank(a) && isNotBlank(b)) return a + " " + b;
        return isNotBlank(a) ? a : (isNotBlank(b) ? b : "");
    }

}