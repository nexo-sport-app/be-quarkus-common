package it.nexoapp.common.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a structured phone number with international and national prefixes.
 * <p>
 * This class should be used every time a phone number needs to be stored, validated,
 * or processed in the application. It provides a standardized way to handle phone
 * numbers by separating them into three components:
 * <ul>
 *   <li><b>International Prefix</b>: Country calling code (e.g., +39 for Italy, +1 for USA)</li>
 *   <li><b>National Prefix</b>: Area or district code (optional, up to 4 digits)</li>
 *   <li><b>Phone Number</b>: The actual phone number (4-25 digits)</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Example 1: Italian mobile number
 * Phone phone1 = Phone.builder()
 *     .internationalPrefix("+39")
 *     .nationalPrefix("340")
 *     .phoneNumber("1234567")
 *     .build();
 *
 * // Example 2: US number with area code
 * Phone phone2 = Phone.builder()
 *     .internationalPrefix("+1")
 *     .nationalPrefix("555")
 *     .phoneNumber("1234567")
 *     .build();
 *
 * // Example 3: Number without national prefix
 * Phone phone3 = Phone.builder()
 *     .internationalPrefix("+44")
 *     .nationalPrefix("")
 *     .phoneNumber("2071234567")
 *     .build();
 * }</pre>
 *
 * <h2>Validation Rules:</h2>
 * <ul>
 *   <li>International prefix must start with '+' followed by 1-3 digits</li>
 *   <li>National prefix is optional but limited to 4 digits maximum</li>
 *   <li>Phone number must be between 4 and 25 digits</li>
 *   <li>Only numeric digits are allowed (no spaces, dashes, or other characters)</li>
 * </ul>
 *
 * <h2>Methods:</h2>
 * <ul>
 *   <li>{@link #getNationalValue()} - Returns phone number with national prefix</li>
 *   <li>{@link #getInternationalValue()} - Returns complete phone number with international prefix</li>
 *   <li>{@link #toString()} - Returns formatted phone number with spaces</li>
 * </ul>
 *
 * @see Serializable
 */
@Builder
@AllArgsConstructor
public class Phone implements Serializable {

    @Serial
    private static final long serialVersionUID = 4114636919292056374L;

    /**
     * The international calling code prefix for the phone number.
     * Must start with '+' followed by 1-3 digits (e.g., +39, +1, +44).
     * This field is required and cannot be blank.
     */
    @NotBlank
    @Pattern(regexp = "^\\+\\d{1,3}$", message = "Use a '+' followed by 1-3 digits (e.g., +39)")
    private String internationalPrefix;

    /**
     * The national or area code prefix for the phone number.
     * This is an optional field that can contain up to 4 digits.
     * Examples: "02" for Milan, "340" for Italian mobile networks.
     */
    @Size(max = 4, message = "Max 4 digits")
    @Pattern(regexp = "^\\d{0,4}$", message = "Only digits allowed")
    private String nationalPrefix;

    /**
     * The actual phone number without prefixes.
     * Must be between 4 and 25 digits and contain only numeric characters.
     * This field is required and cannot be blank.
     */
    @NotBlank
    @Size(min = 4, max = 25)
    @Pattern(regexp = "\\d{4,25}", message = "Only digits allowed")
    private String phoneNumber;

    // --
    // PUBLIC METHODS
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the phone number with national prefix concatenated.
     * Does not include the international prefix.
     *
     * @return the national prefix combined with the phone number (e.g., "3401234567")
     */
    public String getNationalValue() {
        return nationalPrefix + phoneNumber;
    }

    /**
     * Returns the complete phone number in international format.
     * Concatenates all three components: international prefix, national prefix, and phone number.
     *
     * @return the full international phone number (e.g., "+393401234567")
     */
    public String getInternationalValue() {
        return internationalPrefix + nationalPrefix + phoneNumber;
    }

    /**
     * Returns a human-readable string representation of the phone number.
     * The components are separated by spaces for better readability.
     *
     * @return the formatted phone number with spaces (e.g., "+39 340 1234567")
     */
    @Override
    public String toString() {
        return StringUtils.joinWith(" ", internationalPrefix, nationalPrefix, phoneNumber);
    }

}