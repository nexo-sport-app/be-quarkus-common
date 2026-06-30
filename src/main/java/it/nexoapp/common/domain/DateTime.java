package it.nexoapp.common.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import it.nexoapp.common.constant.Language;

import java.io.Serial;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


/**
 * A domain-specific wrapper class for representing date-time values with timezone awareness.
 * This class should be used throughout the application whenever datetime or timestamp values are needed.
 *
 * <h2>Purpose</h2>
 * <p>
 * The {@code DateTime} class provides a consistent, immutable, and timezone-aware representation of
 * date-time values. It wraps Java's {@link Instant} class and provides a domain-friendly API that
 * ensures all temporal values in the application are handled uniformly.
 * </p>
 *
 * <h2>When to Use</h2>
 * <p>
 * Use {@code DateTime} in the following scenarios:
 * </p>
 * <ul>
 *   <li><b>Entity timestamps:</b> For created, updated, or any other timestamp fields in domain entities</li>
 *   <li><b>Event timestamps:</b> For recording when domain events occurred</li>
 *   <li><b>Business logic:</b> For any date-time calculations or comparisons in business logic</li>
 *   <li><b>API requests/responses:</b> For date-time values in DTOs and API contracts</li>
 *   <li><b>Scheduling:</b> For scheduling tasks or storing scheduled execution times</li>
 * </ul>
 *
 * <h2>Benefits</h2>
 * <ul>
 *   <li><b>Timezone safety:</b> All values are stored as UTC internally, avoiding timezone-related bugs</li>
 *   <li><b>Immutability:</b> All operations return new instances, ensuring thread-safety</li>
 *   <li><b>Type safety:</b> Domain-specific type prevents mixing with raw Java time types</li>
 *   <li><b>Serialization:</b> Consistent JSON serialization using ISO-8601 format</li>
 *   <li><b>Convenience:</b> Rich API for common operations (plus, minus, formatting, comparison)</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Creating instances
 * DateTime now = DateTime.now();
 * DateTime parsed = DateTime.parse("2024-01-15T10:30:00Z");
 * DateTime fromInstant = DateTime.of(Instant.now());
 *
 * // Formatting
 * String iso = now.toString();                    // ISO-8601 format
 * String pretty = now.toUI();                     // User-friendly format (dd/MM/yyyy HH:mm)
 * String custom = now.format(customFormatter);    // Custom format
 *
 * // Arithmetic operations
 * DateTime future = now.plusMinutes(30);
 * DateTime past = now.minusSeconds(3600);
 * long secondsBetween = past.until(future, ChronoUnit.SECONDS);
 *
 * // Comparisons
 * if (dateTime1.compareTo(dateTime2) > 0) {
 *     // dateTime1 is after dateTime2
 * }
 *
 * // Conversion
 * Instant instant = now.toInstant();
 * OffsetDateTime odt = now.toOffsetDateTimeUTC();
 * long epochMillis = now.toEpochMillis();
 * }</pre>
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Use {@code DateTime} instead of {@link Instant}, {@link java.time.ZonedDateTime},
 *       or {@link OffsetDateTime} in domain models</li>
 *   <li>Always use {@code DateTime.now()} instead of {@code Instant.now()} for testability</li>
 *   <li>Use {@code toUI()} for displaying datetime values to end users</li>
 *   <li>Use {@code toString()} for API responses and logging (produces ISO-8601 format)</li>
 *   <li>For testing, inject a {@link Clock} and use {@code DateTime.now(clock)} for time-dependent logic</li>
 * </ul>
 *
 * <h2>JSON Serialization</h2>
 * <p>
 * The class is annotated with {@code @JsonValue} on the {@code toString()} method, ensuring
 * automatic serialization to ISO-8601 format in JSON responses.
 * </p>
 *
 * @see Instant
 * @see DateTimeFormatter
 * @see ZoneOffset
 */
public class DateTime implements Comparable<DateTime>, Serializable {

    @Serial
    private static final long serialVersionUID = 6541794329433390408L;

    /** A constant representing the default time zone offset used in the application. */
    public static final ZoneOffset DEFAULT_ZONE = ZoneOffset.UTC;

    /**
     * A default {@link DateTimeFormatter} used to format or parse date-time values
     * in ISO-8601 extended format with an offset, such as "2011-12-03T10:15:30+01:00".
     * This formatter ensures consistency in handling date-time values across the application.
     */
    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withLocale(Language.getDefault().getLocale());

    /**
     * A predefined {@link DateTimeFormatter} for formatting date-time values
     * in a human-readable "pretty" format.
     * <p>
     * The format pattern used is "dd/MM/yyyy HH:mm", where:
     * - "dd" represents the day of the month as a two-digit number.
     * - "MM" represents the month as a two-digit number.
     * - "yyyy" represents the year as a four-digit number.
     * - "HH:mm" represents the hour and minute in 24-hour time.
     */
    public static final DateTimeFormatter PRETTY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OffsetDateTime value;

    // --
    // CONSTRUCTOR
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a DateTime from an OffsetDateTime.
     *
     * @param offsetDateTime the OffsetDateTime to use (must not be null)
     */
    private DateTime(final OffsetDateTime offsetDateTime) {
        this.value = Objects.requireNonNull(offsetDateTime, "instant");
    }

    /**
     * Constructs a DateTime from an Instant.
     *
     * @param instant the instant to use (must not be null)
     */
    private DateTime(final Instant instant) {
        this.value = Objects.requireNonNull(instant.atOffset(DEFAULT_ZONE), "instant");
    }

    /**
     * Constructs a DateTime from a string representation.
     *
     * @param value the date time string in ISO-8601 format
     */
    public DateTime(final String value) {
        this.value = OffsetDateTime.parse(value);
    }

    /**
     * Constructs a DateTime using the provided date-time string and formatter.
     *
     * @param value the date-time string to parse
     * @param format   the DateTimeFormatter to use for parsing
     */
    public DateTime(final String value, final DateTimeFormatter format){
        this.value = OffsetDateTime.from(format.parse(value));
    }

    // --
    // STATIC METHOD
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a DateTime representing the current instant.
     *
     * @return a new DateTime instance
     */
    public static DateTime now() {
        return new DateTime(Instant.now());
    }

    /**
     * Creates a DateTime representing the current instant using the specified clock.
     *
     * @param clock the clock to use
     * @return a new DateTime instance
     */
    public static DateTime now(final Clock clock) {
        return new DateTime(Instant.now(clock));
    }

    /**
     * Creates a DateTime from an Instant.
     *
     * @param instant the instant to use
     * @return a new DateTime instance
     */
    public static DateTime of(final Instant instant) {
        return new DateTime(instant);
    }

    /**
     * Creates a DateTime from an OffsetDateTime.
     *
     * @param odt the OffsetDateTime to convert
     * @return a new DateTime instance
     */
    public static DateTime of(final OffsetDateTime odt) {
        return new DateTime(odt.toInstant());
    }

    /**
     * Parses a date-time string in ISO-8601 format.
     *
     * @param iso8601 the date-time string to parse
     * @return a new DateTime instance
     */
    public static DateTime parse(final String iso8601) {
        return new DateTime(Instant.parse(iso8601));
    }

    // --
    // INSTANCE METHOD
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Formats this datetime using the specified {@link DateTimeFormatter}.
     *
     * @param format the formatter to be used for formatting the datetime
     * @return the formatted datetime string
     */
    public String format(final DateTimeFormatter format){
        return format.format(toOffsetDateTimeUTC());
    }

    /**
     * Returns the underlying Instant.
     *
     * @return the instant
     */
    public Instant toInstant() {
        return value.toInstant();
    }

    /**
     * Converts this datetime to an {@link OffsetDateTime} using the specified {@link ZoneOffset}.
     *
     * @param zone the zone offset to apply (must not be null)
     * @return an {@link OffsetDateTime} representation of this datetime in the specified zone offset
     */
    public OffsetDateTime toOffsetDateTime(final ZoneOffset zone) {
        return value.withOffsetSameLocal(zone);
    }

    /**
     * Converts this datetime to an OffsetDateTime in UTC.
     *
     * @return the OffsetDateTime in UTC
     */
    public OffsetDateTime toOffsetDateTimeUTC() {
        return value;
    }

    /**
     * Returns the number of seconds from the epoch of 1970-01-01T00:00:00Z.
     *
     * @return the number of seconds from the epoch
     */
    public long toEpochSeconds() {
        return value.toEpochSecond();
    }

    /**
     * Returns the number of milliseconds from the epoch of 1970-01-01T00:00:00Z.
     *
     * @return the number of milliseconds from the epoch
     */
    public long toEpochMillis() {
        return value.toInstant().toEpochMilli();
    }

    /**
     * Returns a copy of this datetime with the specified duration added.
     *
     * @param d the duration to add
     * @return a new DateTime with the duration added
     */
    public DateTime plus(final Duration d) {
        return new DateTime(value.plus(d));
    }

    /**
     * Returns a copy of this datetime with the specified number of milliseconds added.
     *
     * @param millis the milliseconds to add
     * @return a new DateTime with the milliseconds added
     */
    public DateTime plusMillis(final long millis) {
        return new DateTime(value.plus(Duration.ofMillis(millis)));
    }

    /**
     * Returns a copy of this datetime with the specified number of seconds added.
     *
     * @param seconds the seconds to add
     * @return a new DateTime with the seconds added
     */
    public DateTime plusSeconds(final long seconds) {
        return new DateTime(value.plus(Duration.ofSeconds(seconds)));
    }

    /**
     * Returns a copy of this datetime with the specified number of minutes added.
     *
     * @param minutes the minutes to add
     * @return a new DateTime with the minutes added
     */
    public DateTime plusMinutes(final long minutes) {
        return new DateTime(value.plus(Duration.ofMinutes(minutes)));
    }

    /**
     * Returns a copy of this datetime with the specified duration subtracted.
     *
     * @param d the duration to subtract
     * @return a new DateTime with the duration subtracted
     */
    public DateTime minus(final Duration d) {
        return new DateTime(value.minus(d));
    }

    /**
     * Returns a copy of this datetime with the specified number of milliseconds subtracted.
     *
     * @param millis the milliseconds to subtract
     * @return a new DateTime with the milliseconds subtracted
     */
    public DateTime minusMillis(final long millis) {
        return new DateTime(value.minus(Duration.ofMillis(millis)));
    }

    /**
     * Returns a copy of this datetime with the specified number of seconds subtracted.
     *
     * @param seconds the seconds to subtract
     * @return a new DateTime with the seconds subtracted
     */
    public DateTime minusSeconds(final long seconds) {
        return new DateTime(value.minus(Duration.ofSeconds(seconds)));
    }

    /**
     * Returns a copy of this datetime with the specified number of minutes subtracted.
     *
     * @param minutes the minutes to subtract
     * @return a new DateTime with the minutes subtracted
     */
    public DateTime minusMinutes(final long minutes) {
        return new DateTime(value.minus(Duration.ofMinutes(minutes)));
    }

    /**
     * Calculates the amount of time until another datetime in the specified unit.
     *
     * @param other the end datetime
     * @param unit  the unit to measure the amount in
     * @return the amount of time between datetimes in the specified unit
     */
    public long until(final DateTime other, final ChronoUnit unit) {
        return unit.between(this.value, other.value);
    }

    /**
     * Converts this datetime to a Date in the specified time zone.
     *
     * @param zone the time zone
     * @return a new Date
     */
    public Date toDomainDate(final ZoneId zone) {
        return Date.of(value.toLocalDate().atStartOfDay(zone).toLocalDate());
    }

    /**
     * Formats this datetime in a pretty format (dd/MM/yyyy HH:mm).
     *
     * @return the formatted datetime string
     */
    public String toUI(){
        return PRETTY_FORMAT.format(toOffsetDateTimeUTC());
    }

    // --
    // OVERRIDES
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int compareTo(final DateTime o) {
        return this.value.compareTo(o.value);
    }

    @JsonValue
    @Override
    public String toString() {
        return DEFAULT_FORMAT.format(toOffsetDateTimeUTC());
    }

    @Override public
    boolean equals(final Object o){
        return (o instanceof DateTime d) && value.equals(d.value);
    }

    @Override
    public int hashCode(){
        return value.hashCode();
    }

}