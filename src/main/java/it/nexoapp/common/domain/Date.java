package it.nexoapp.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.nexoapp.common.constant.Language;

import java.io.Serial;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * A domain-specific wrapper class for representing dates without time information.
 *
 * <p>This class provides a type-safe, immutable wrapper around {@link LocalDate} designed for
 * use in domain models and DTOs. It should be used instead of {@link LocalDate} directly to:
 * <ul>
 *   <li>Enforce consistent date formatting across the application (ISO 8601 format by default)</li>
 *   <li>Provide domain-specific date operations and conversions</li>
 *   <li>Ensure proper JSON serialization/deserialization with Jackson</li>
 *   <li>Maintain serialization compatibility across service boundaries</li>
 * </ul>
 *
 * <h2>When to Use This Class</h2>
 * <p>Use {@code Date} whenever you need to represent a date in:
 * <ul>
 *   <li>Domain entities and value objects</li>
 *   <li>DTOs (Data Transfer Objects) for REST APIs</li>
 *   <li>Service layer interfaces</li>
 *   <li>Any context where dates need to be serialized/deserialized</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Create a date for today
 * Date today = Date.today();
 *
 * // Parse from ISO format string (yyyy-MM-dd)
 * Date date = new Date("2024-03-15");
 * Date dateParsed = Date.parse("2024-03-15");
 *
 * // Create from LocalDate
 * Date date = Date.of(LocalDate.now());
 *
 * // Format for display
 * String iso = date.format();              // "2024-03-15"
 * String pretty = date.toUI();              // "15/03/2024"
 *
 * // Date arithmetic
 * Date nextMonth = date.plusMonths(1);
 * Date lastYear = date.minusYears(1);
 * Date endOfMonth = date.endOfMonth();
 *
 * // Convert to DateTime
 * DateTime startOfDay = date.atStartOfDay();
 * DateTime endOfDay = date.atEndOfDay();
 *
 * // Calculate differences
 * long daysBetween = date1.until(date2, ChronoUnit.DAYS);
 * }</pre>
 *
 * <h2>Immutability</h2>
 * <p>This class is immutable and thread-safe. All operations that modify the date return
 * a new {@code Date} instance rather than modifying the existing one.
 *
 * <h2>Serialization</h2>
 * <p>The class implements {@link Serializable} and provides proper Jackson JSON
 * serialization/deserialization support. Dates are serialized as ISO 8601 formatted
 * strings (yyyy-MM-dd) by default.
 *
 * @see LocalDate
 * @see DateTime
 * @see DateTimeFormatter
 */
public class Date implements Serializable {

    @Serial
    private static final long serialVersionUID = -7853504476617787149L;

    /**
     * The default date-time formatter for the {@code Date} class.
     * It follows the ISO-8601 standard's local date format (yyyy-MM-dd).
     * Used for parsing and formatting dates in a consistent and standardized manner.
     */
    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE
            .withLocale(Language.getDefault().getLocale());

    /**
     * A predefined DateTimeFormatter for formatting and parsing dates in the "dd/MM/yyyy" pattern.
     * Typically used to represent dates in a human-readable "day/month/year" format.
     */
    public static final DateTimeFormatter PRETTY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Represents the underlying date value of this {@code Date} instance.
     * This field stores a {@link LocalDate} object, which encapsulates the actual
     * date information (year, month, day) without any time or time zone component.
     * It is immutable and thread-safe.
     */
    private final LocalDate value;

    // --
    // CONSTRUCTOR
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a DomainDate with the current date.
     */
    public Date(){
        this.value = LocalDate.now();
    }

    /**
     * Constructs a DomainDate from a string in ISO format (yyyy-MM-dd).
     *
     * @param yyyy_MM_dd the date string in ISO format
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Date(final String yyyy_MM_dd) {
        this.value = LocalDate.parse(yyyy_MM_dd, DEFAULT_FORMAT);
    }

    /**
     * Constructs a DomainDate from a string using the specified formatter.
     *
     * @param date   the date string
     * @param format the formatter to parse the date
     */
    public Date(final String date, final DateTimeFormatter format){
        this.value = LocalDate.parse(date, format);
    }

    /**
     * Constructs a DomainDate from a string using the specified format pattern.
     *
     * @param date   the date string
     * @param format the format pattern
     */
    public Date(final String date, final String format){
        this(date, DateTimeFormatter.ofPattern(format));
    }

    /**
     * Constructs a DomainDate from a LocalDate.
     *
     * @param date the LocalDate (must not be null)
     */
    public Date(final LocalDate date){
        this.value = Objects.requireNonNull(date, "value");
    }

    /**
     * Constructs a DomainDate from an OffsetDateTime.
     *
     * @param dateTime the OffsetDateTime
     */
    public Date(final OffsetDateTime dateTime){
        this.value = dateTime.toLocalDate();
    }

    /**
     * Constructs a new Date instance from the specified {@link Instant}.
     *
     * @param instant the instant to convert into a LocalDate, must not be null
     */
    public Date(final Instant instant){
        this.value = instant.atZone(DateTime.DEFAULT_ZONE).toLocalDate();
    }

    // --
    // STATIC METHOD
    // -----------------------------------------------------------------------------------------------------------------
    
    /**
     * Creates a DomainDate from a LocalDate.
     *
     * @param date the LocalDate
     * @return a new DomainDate instance
     */
    public static Date of(final LocalDate date) {
        return new Date(date);
    }

    /**
     * Creates a new Date instance from the specified {@link Instant}.
     *
     * @param instant the instant to convert into a Date; must not be null
     * @return a new Date instance representing the given instant
     */
    public static Date of(final Instant instant) {
        return new Date(instant);
    }

    /**
     * Parses a date string in the default ISO format.
     *
     * @param text the date string to parse
     * @return a new DomainDate instance
     */
    public static Date parse(final String text) {
        return new Date(LocalDate.parse(text, DEFAULT_FORMAT));
    }

    /**
     * Creates a DomainDate representing today's date.
     *
     * @return a new DomainDate instance for today
     */
    public static Date today(){
        return new Date();
    }

    // --
    // INSTANCE METHOD
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Converts this instance to a LocalDate.
     *
     * @return the LocalDate value represented by this instance
     */
    public LocalDate toLocalDate() {
        return value;
    }

    /**
     * Returns the underlying LocalDate value.
     *
     * @return the LocalDate value
     */
    public LocalDate value() {
        return value;
    }

    /**
     * Formats the date using the default ISO format.
     *
     * @return the formatted date string
     */
    public String format() {
        return value.format(DEFAULT_FORMAT);
    }

    /**
     * Formats the date using the specified formatter.
     *
     * @param fmt the formatter to use
     * @return the formatted date string
     */
    public String format(final DateTimeFormatter fmt) {
        return value.format(fmt);
    }

    /**
     * Adds a period to this date.
     *
     * @param p the period to add
     * @return a new DomainDate with the period added
     */
    public Date plus(final Period p) {
        return new Date(value.plus(p));
    }

    /**
     * Subtracts a period from this date.
     *
     * @param p the period to subtract
     * @return a new DomainDate with the period subtracted
     */
    public Date minus(final Period p) {
        return new Date(value.minus(p));
    }

    /**
     * Returns a new DomainDate representing the last day of the current month.
     *
     * @return a new DomainDate for the last day of the month
     */
    public Date endOfMonth() {
        return new Date(value.withDayOfMonth(value.lengthOfMonth()));
    }

    /**
     * Converts this date to a DomainDateTime at the start of the day in the specified time zone.
     *
     * @param zone the time zone
     * @return a new DomainDateTime at the start of the day
     */
    public DateTime atStartOfDay(final ZoneId zone) {
        return DateTime.of(value.atStartOfDay(zone).toInstant());
    }

    /**
     * Converts this date to a DomainDateTime at the start of the day in the specified time zone.
     *
     * @return a new DomainDateTime at the start of the day
     */
    public DateTime atStartOfDay() {
        return DateTime.of(value.atStartOfDay(DateTime.DEFAULT_ZONE).toInstant());
    }

    /**
     * Converts this date to a DomainDateTime at the end of the day in the specified time zone.
     *
     * @param zone the time zone
     * @return a new DomainDateTime at the end of the day
     */
    public DateTime atEndOfDay(final ZoneId zone) {
        return DateTime.of(value.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant());
    }

    /**
     * Converts this date to a DomainDateTime at the end of the day in the specified time zone.
     *
     * @return a new DomainDateTime at the end of the day
     */
    public DateTime atEndOfDay() {
        return DateTime.of(value.plusDays(1)
                .atStartOfDay(DateTime.DEFAULT_ZONE)
                .minusNanos(1)
                .toInstant());
    }

    /**
     * Adds the specified number of days to this date.
     *
     * @param days the number of days to add
     * @return a new Date instance with the days added
     */
    public Date plusDays(final int days) {
        return plus(Period.ofDays(days));
    }

    /**
     * Adds the specified number of months to this date.
     *
     * @param months the number of months to add
     * @return a new DomainDate with the months added
     */
    public Date plusMonths(final int months) {
        return plus(Period.ofMonths(months));
    }

    /**
     * Adds the specified number of years to this date.
     *
     * @param years the number of years to add
     * @return a new DomainDate with the years added
     */
    public Date plusYears(final int years) {
        return plus(Period.ofYears(years));
    }


    /**
     * Subtracts the specified number of days from this date.
     *
     * @param days the number of days to subtract
     * @return a new Date instance with the days subtracted
     */
    public Date minusDays(final int days) {
        return minus(Period.ofDays(days));
    }

    /**
     * Subtracts the specified number of months from this date.
     *
     * @param months the number of months to subtract
     * @return a new DomainDate with the months subtracted
     */
    public Date minusMonths(final int months) {
        return minus(Period.ofMonths(months));
    }

    /**
     * Subtracts the specified number of years from this date.
     *
     * @param years the number of years to subtract
     * @return a new DomainDate with the years subtracted
     */
    public Date minusYears(final int years) {
        return minus(Period.ofYears(years));
    }

    /**
     * Calculates the amount of time until another date in the specified unit.
     *
     * @param other the end date
     * @param unit  the unit to measure the amount in
     * @return the amount of time between dates in the specified unit
     */
    public long until(final Date other, final ChronoUnit unit) {
        return unit.between(this.value, other.value);
    }

    /**
     * Formats the date in a pretty format (dd/MM/yyyy).
     *
     * @return the formatted date string
     */
    public String toUI(){
        return value.format(PRETTY_FORMAT);
    }

    // --
    // OVERRIDES
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the string representation of this date in ISO format.
     *
     * @return the formatted date string
     */
    @JsonValue
    @Override
    public String toString(){
        return value.format(DEFAULT_FORMAT);
    }

    /**
     * Checks if this date is equal to another object.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        return (o instanceof Date d) && value.equals(d.value);
    }

    /**
     * Returns the hash code for this date.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

}