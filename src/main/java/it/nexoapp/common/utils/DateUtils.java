package it.nexoapp.common.utils;


import it.nexoapp.common.domain.Date;
import it.nexoapp.common.domain.DateTime;
import it.nexoapp.common.exception.IllegalInstantiationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for performing operations related to date and time.
 * Provides methods for epoch conversions, date string reformatting,
 * calculating differences between dates, and expiration date calculations.
 */
public class DateUtils {

    private DateUtils(){
        throw new IllegalInstantiationException();
    }

    // --
    // Epoch conversions
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Converts epoch time in milliseconds to a DateTime object.
     * <p>
     * This method takes a Unix timestamp in milliseconds (time since January 1, 1970, 00:00:00 GMT)
     * and converts it to a DateTime representation.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert epoch milliseconds to DateTime
     * long epochMillis = 1705334400000L; // January 15, 2024, 12:00:00 PM UTC
     * DateTime dateTime = DateUtils.fromEpochMillis(epochMillis);
     * // Returns: DateTime representing 2024-01-15T12:00:00Z
     *
     * // Example 2: Convert current time
     * long currentMillis = System.currentTimeMillis();
     * DateTime now = DateUtils.fromEpochMillis(currentMillis);
     * // Returns: DateTime representing the current instant
     *
     * // Example 3: Convert specific timestamp
     * long specificTime = 0L; // Unix epoch start
     * DateTime epoch = DateUtils.fromEpochMillis(specificTime);
     * // Returns: DateTime representing 1970-01-01T00:00:00Z
     * }</pre>
     *
     * @param epochMillis the epoch time in milliseconds
     * @return DateTime representation of the epoch time
     */
    public static DateTime fromEpochMillis(final long epochMillis) {
        return DateTime.of(Instant.ofEpochMilli(epochMillis));
    }

    /**
     * Converts epoch time in seconds to a DateTime object.
     * <p>
     * This method takes a Unix timestamp in seconds (time since January 1, 1970, 00:00:00 GMT)
     * and converts it to a DateTime representation.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert epoch seconds to DateTime
     * long epochSeconds = 1705334400L; // January 15, 2024, 12:00:00 PM UTC
     * DateTime dateTime = DateUtils.fromEpochSeconds(epochSeconds);
     * // Returns: DateTime representing 2024-01-15T12:00:00Z
     *
     * // Example 2: Convert Unix epoch start
     * long epochStart = 0L;
     * DateTime epoch = DateUtils.fromEpochSeconds(epochStart);
     * // Returns: DateTime representing 1970-01-01T00:00:00Z
     *
     * // Example 3: Convert specific date
     * long specificTime = 1609459200L; // January 1, 2021, 00:00:00 UTC
     * DateTime newYear = DateUtils.fromEpochSeconds(specificTime);
     * // Returns: DateTime representing 2021-01-01T00:00:00Z
     * }</pre>
     *
     * @param epochSeconds the epoch time in seconds
     * @return DateTime representation of the epoch time
     */
    public static DateTime fromEpochSeconds(final long epochSeconds) {
        return DateTime.of(Instant.ofEpochSecond(epochSeconds));
    }

    /**
     * Converts epoch time in milliseconds to a Date object.
     * <p>
     * This method takes a Unix timestamp in milliseconds (time since January 1, 1970, 00:00:00 GMT)
     * and converts it to a Date representation, discarding the time component.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert epoch milliseconds to Date
     * long epochMillis = 1705334400000L; // January 15, 2024, 12:00:00 PM UTC
     * Date date = DateUtils.dateFromEpochMillis(epochMillis);
     * // Returns: Date representing 2024-01-15
     *
     * // Example 2: Convert current time to date
     * long currentMillis = System.currentTimeMillis();
     * Date today = DateUtils.dateFromEpochMillis(currentMillis);
     * // Returns: Date representing today
     *
     * // Example 3: Convert specific timestamp
     * long specificTime = 0L; // Unix epoch start
     * Date epoch = DateUtils.dateFromEpochMillis(specificTime);
     * // Returns: Date representing 1970-01-01
     * }</pre>
     *
     * @param epochMillis the epoch time in milliseconds
     * @return Date representation of the epoch time (without time component)
     */
    public static Date dateFromEpochMillis(final long epochMillis) {
        return Date.of(Instant.ofEpochMilli(epochMillis));
    }

    /**
     * Converts epoch time in seconds to a Date object.
     * <p>
     * This method takes a Unix timestamp in seconds (time since January 1, 1970, 00:00:00 GMT)
     * and converts it to a Date representation, discarding the time component.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert epoch seconds to Date
     * long epochSeconds = 1705334400L; // January 15, 2024, 12:00:00 PM UTC
     * Date date = DateUtils.dateFromEpochSeconds(epochSeconds);
     * // Returns: Date representing 2024-01-15
     *
     * // Example 2: Convert Unix epoch start
     * long epochStart = 0L;
     * Date epoch = DateUtils.dateFromEpochSeconds(epochStart);
     * // Returns: Date representing 1970-01-01
     *
     * // Example 3: Convert specific date
     * long specificTime = 1609459200L; // January 1, 2021, 00:00:00 UTC
     * Date newYear = DateUtils.dateFromEpochSeconds(specificTime);
     * // Returns: Date representing 2021-01-01
     * }</pre>
     *
     * @param epochSeconds the epoch time in seconds
     * @return Date representation of the epoch time (without time component)
     */
    public static Date dateFromEpochSeconds(final long epochSeconds) {
        return Date.of(Instant.ofEpochSecond(epochSeconds));
    }

    // --
    // String to String
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Reformats a given date and time string from one format to another.
     * <p>
     * This method parses the input date-time string using the source formatter and converts it
     * to the target format. Useful for converting between different date-time representations.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert ISO format to custom format
     * String isoDateTime = "2024-01-15T14:30:00";
     * DateTimeFormatter from = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
     * DateTimeFormatter to = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
     * String result = DateUtils.reformatDateTime(isoDateTime, from, to);
     * // Returns: "15/01/2024 14:30"
     *
     * // Example 2: Convert US format to European format
     * String usDateTime = "01-15-2024 02:30 PM";
     * DateTimeFormatter fromUS = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");
     * DateTimeFormatter toEU = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
     * String result = DateUtils.reformatDateTime(usDateTime, fromUS, toEU);
     * // Returns: "15.01.2024 14:30"
     *
     * // Example 3: Convert custom format to ISO
     * String custom = "15/01/2024 14:30:00";
     * DateTimeFormatter fromCustom = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
     * DateTimeFormatter toISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
     * String result = DateUtils.reformatDateTime(custom, fromCustom, toISO);
     * // Returns: "2024-01-15T14:30:00"
     * }</pre>
     *
     * @param dateTime the date and time string to be reformatted; must not be null
     * @param from the source {@code DateTimeFormatter} that describes the format of the input dateTime; must not be null
     * @param to the target {@code DateTimeFormatter} that describes the desired format of the output dateTime; must not be null
     * @return the reformatted date and time string in the specified target format
     * @throws IllegalArgumentException if any of the parameters (dateTime, from, or to) are null
     */
    public static String reformatDateTime(final String dateTime, final DateTimeFormatter from, final DateTimeFormatter to) {
        if (dateTime == null || from == null || to == null) {
            throw new IllegalArgumentException("Date, from formatter and to formatter cannot be null");
        }
        return new DateTime(dateTime, from).format(to);
    }

    /**
     * Reformats a date-time string from the specified format to the default DateTime format.
     * <p>
     * This method parses the input date-time string using the provided formatter and converts it
     * to the default format defined in {@link DateTime#DEFAULT_FORMAT}.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert custom format to default
     * String customDateTime = "15/01/2024 14:30:00";
     * DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
     * String result = DateUtils.reformatDateTime(customDateTime, customFormatter);
     * // Returns: date-time in DateTime.DEFAULT_FORMAT
     *
     * // Example 2: Convert US format to default
     * String usDateTime = "01-15-2024 02:30 PM";
     * DateTimeFormatter usFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");
     * String result = DateUtils.reformatDateTime(usDateTime, usFormatter);
     * // Returns: date-time in DateTime.DEFAULT_FORMAT
     *
     * // Example 3: Convert ISO format to default
     * String isoDateTime = "2024-01-15T14:30:00";
     * String result = DateUtils.reformatDateTime(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
     * // Returns: date-time in DateTime.DEFAULT_FORMAT
     * }</pre>
     *
     * @param dateTime the date-time string to be reformatted; must not be null
     * @param from the {@code DateTimeFormatter} representing the format of the input date-time string; must not be null
     * @return the reformatted date-time string in the default format
     * @throws IllegalArgumentException if dateTime or from is null
     * @see #reformatDateTime(String, DateTimeFormatter, DateTimeFormatter)
     * @see DateTime#DEFAULT_FORMAT
     */
    public static String reformatDateTime(final String dateTime, final DateTimeFormatter from) {
        return reformatDateTime(dateTime, from, DateTime.DEFAULT_FORMAT);
    }
    
    /**
     * Reformats a date string from one format to another.
     * <p>
     * This method parses the input date string using the source formatter and converts it
     * to the target format. Useful for converting between different date representations.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert ISO format to European format
     * String isoDate = "2024-01-15";
     * DateTimeFormatter from = DateTimeFormatter.ISO_LOCAL_DATE;
     * DateTimeFormatter to = DateTimeFormatter.ofPattern("dd/MM/yyyy");
     * String result = DateUtils.reformatDate(isoDate, from, to);
     * // Returns: "15/01/2024"
     *
     * // Example 2: Convert US format to ISO format
     * String usDate = "01-15-2024";
     * DateTimeFormatter fromUS = DateTimeFormatter.ofPattern("MM-dd-yyyy");
     * DateTimeFormatter toISO = DateTimeFormatter.ISO_LOCAL_DATE;
     * String result = DateUtils.reformatDate(usDate, fromUS, toISO);
     * // Returns: "2024-01-15"
     *
     * // Example 3: Convert European to US format
     * String euroDate = "15.01.2024";
     * DateTimeFormatter fromEuro = DateTimeFormatter.ofPattern("dd.MM.yyyy");
     * DateTimeFormatter toUS = DateTimeFormatter.ofPattern("MM/dd/yyyy");
     * String result = DateUtils.reformatDate(euroDate, fromEuro, toUS);
     * // Returns: "01/15/2024"
     * }</pre>
     *
     * @param date the date string to be reformatted; must not be null
     * @param from the formatter for parsing the input date; must not be null
     * @param to the formatter for formatting the output date; must not be null
     * @return the reformatted date string in the target format
     * @throws IllegalArgumentException if the date, from formatter, or to formatter is null
     */
    public static String reformatDate(final String date, final DateTimeFormatter from, final DateTimeFormatter to) {
        if (date == null || from == null || to == null) {
            throw new IllegalArgumentException("Date, from formatter and to formatter cannot be null");
        }
        return new Date(date, from).format(to);
    }

    /**
     * Reformats a given date string from the specified input format to the default date output format.
     * <p>
     * This method parses the input date string using the provided formatter and converts it to
     * the default date format defined in {@link Date#DEFAULT_FORMAT}.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert ISO date format to default format
     * String isoDate = "2024-01-15";
     * String result = DateUtils.reformatDate(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
     * // Returns date in Date.DEFAULT_FORMAT
     *
     * // Example 2: Convert custom date format to default format
     * String customDate = "15/01/2024";
     * DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
     * String result = DateUtils.reformatDate(customDate, customFormatter);
     * // Returns date in Date.DEFAULT_FORMAT
     *
     * // Example 3: Convert US date format to default format
     * String usDate = "01-15-2024";
     * DateTimeFormatter usFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
     * String result = DateUtils.reformatDate(usDate, usFormatter);
     * // Returns date in Date.DEFAULT_FORMAT
     * }</pre>
     *
     * @param date the date string to be reformatted; must not be null
     * @param from the DateTimeFormatter representing the format of the input date string; must not be null
     * @return the reformatted date string in the default output format
     * @throws IllegalArgumentException if the date or from formatter is null
     * @see #reformatDate(String, DateTimeFormatter, DateTimeFormatter)
     * @see Date#DEFAULT_FORMAT
     */
    public static String reformatDate(final String date, final DateTimeFormatter from) {
        return reformatDate(date, from, Date.DEFAULT_FORMAT);
    }

    /**
     * Reformats a date string from the specified format to a human-readable "pretty" format.
     * <p>
     * This method parses the input date string using the provided formatter and converts it
     * to the pretty format defined in {@link Date#PRETTY_FORMAT}, which is typically more
     * user-friendly and readable.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert ISO date to pretty format
     * String isoDate = "2024-01-15";
     * String result = DateUtils.reformatToPrettyDate(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
     * // Returns: date in Date.PRETTY_FORMAT (e.g., "January 15, 2024")
     *
     * // Example 2: Convert US format to pretty format
     * String usDate = "01-15-2024";
     * DateTimeFormatter usFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
     * String result = DateUtils.reformatToPrettyDate(usDate, usFormatter);
     * // Returns: date in Date.PRETTY_FORMAT (e.g., "January 15, 2024")
     *
     * // Example 3: Convert compact format to pretty format
     * String compactDate = "20240115";
     * DateTimeFormatter compactFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
     * String result = DateUtils.reformatToPrettyDate(compactDate, compactFormatter);
     * // Returns: date in Date.PRETTY_FORMAT (e.g., "January 15, 2024")
     * }</pre>
     *
     * @param date the date string to be reformatted; must not be null
     * @param from the formatter for parsing the input date; must not be null
     * @return the reformatted date string in the pretty date format
     * @throws IllegalArgumentException if the date or from formatter is null
     * @see #reformatDate(String, DateTimeFormatter, DateTimeFormatter)
     * @see Date#PRETTY_FORMAT
     */
    public static String reformatToPrettyDate(final String date, final DateTimeFormatter from) {
        return reformatDate(date, from, Date.PRETTY_FORMAT);
    }

    /**
     * Reformats a date-time string from the specified format to a human-readable "pretty" format.
     * <p>
     * This method parses the input date-time string using the provided formatter and converts it
     * to the pretty format defined in {@link DateTime#PRETTY_FORMAT}, which is typically more
     * user-friendly and readable, often including the full month name and time.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Convert ISO date-time to pretty format
     * String isoDateTime = "2024-01-15T14:30:00";
     * String result = DateUtils.reformatToPrettyDateTime(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
     * // Returns: date-time in DateTime.PRETTY_FORMAT (e.g., "January 15, 2024 at 2:30 PM")
     *
     * // Example 2: Convert custom format to pretty format
     * String customDateTime = "15/01/2024 14:30";
     * DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
     * String result = DateUtils.reformatToPrettyDateTime(customDateTime, customFormatter);
     * // Returns: date-time in DateTime.PRETTY_FORMAT (e.g., "January 15, 2024 at 2:30 PM")
     *
     * // Example 3: Convert compact format to pretty format
     * String compactDateTime = "20240115143000";
     * DateTimeFormatter compactFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
     * String result = DateUtils.reformatToPrettyDateTime(compactDateTime, compactFormatter);
     * // Returns: date-time in DateTime.PRETTY_FORMAT (e.g., "January 15, 2024 at 2:30 PM")
     * }</pre>
     *
     * @param dateTime the date-time string to be reformatted; must not be null
     * @param from the formatter for parsing the input date-time; must not be null
     * @return the reformatted date-time string in the pretty date-time format
     * @throws IllegalArgumentException if the dateTime or from formatter is null
     * @see #reformatDateTime(String, DateTimeFormatter, DateTimeFormatter)
     * @see DateTime#PRETTY_FORMAT
     */
    public static String reformatToPrettyDateTime(final String dateTime, final DateTimeFormatter from) {
        return reformatDateTime(dateTime, from, DateTime.PRETTY_FORMAT);
    }

    // --
    // Difference between date
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Calculates the difference between two DateTime objects in the specified time unit.
     * <p>
     * This method computes the temporal difference between two dates using the provided ChronoUnit.
     * Both dates are converted to UTC for accurate calculation across time zones.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Calculate difference in hours
     * DateTime start = DateTime.of("2024-01-15T10:00:00");
     * DateTime end = DateTime.of("2024-01-15T15:30:00");
     * BigDecimal hours = DateUtils.differenceBetween(start, end, ChronoUnit.HOURS);
     * // Returns: 5
     *
     * // Example 2: Calculate difference in days
     * DateTime date1 = DateTime.of("2024-01-15T00:00:00");
     * DateTime date2 = DateTime.of("2024-01-20T00:00:00");
     * BigDecimal days = DateUtils.differenceBetween(date1, date2, ChronoUnit.DAYS);
     * // Returns: 5
     *
     * // Example 3: Calculate difference in minutes
     * DateTime startTime = DateTime.of("2024-01-15T10:00:00");
     * DateTime endTime = DateTime.of("2024-01-15T10:45:00");
     * BigDecimal minutes = DateUtils.differenceBetween(startTime, endTime, ChronoUnit.MINUTES);
     * // Returns: 45
     * }</pre>
     *
     * @param earliest the earlier date; must not be null
     * @param latest   the later date; must not be null
     * @param unit     the unit to measure the difference in (e.g., DAYS, HOURS, MINUTES); must not be null
     * @return the difference between dates in specified unit as a BigDecimal
     * @throws IllegalArgumentException if any parameter is null
     */
    public static BigDecimal differenceBetween(final DateTime earliest, final DateTime latest, final ChronoUnit unit) {
        if (earliest == null || latest == null || unit == null) {
            throw new IllegalArgumentException("Earliest date, latest date and unit cannot be null");
        }
        final long difference = unit.between(earliest.toOffsetDateTimeUTC(), latest.toOffsetDateTimeUTC());
        return BigDecimal.valueOf(difference);
    }

    /**
     * Calculates the difference between two Date objects in the specified time unit.
     * <p>
     * This method computes the temporal difference between two dates using the provided ChronoUnit.
     * Only date-based units (DAYS, MONTHS, YEARS, etc.) are meaningful for Date objects.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Calculate difference in days
     * Date start = Date.of("2024-01-15");
     * Date end = Date.of("2024-01-20");
     * BigDecimal days = DateUtils.differenceBetween(start, end, ChronoUnit.DAYS);
     * // Returns: 5
     *
     * // Example 2: Calculate difference in months
     * Date date1 = Date.of("2024-01-15");
     * Date date2 = Date.of("2024-04-15");
     * BigDecimal months = DateUtils.differenceBetween(date1, date2, ChronoUnit.MONTHS);
     * // Returns: 3
     *
     * // Example 3: Calculate difference in years
     * Date startDate = Date.of("2020-01-15");
     * Date endDate = Date.of("2024-01-15");
     * BigDecimal years = DateUtils.differenceBetween(startDate, endDate, ChronoUnit.YEARS);
     * // Returns: 4
     * }</pre>
     *
     * @param earliest the earlier date; must not be null
     * @param latest   the later date; must not be null
     * @param unit     the unit to measure the difference in (e.g., DAYS, MONTHS, YEARS); must not be null
     * @return the difference between dates in specified unit as a BigDecimal
     * @throws IllegalArgumentException if any parameter is null
     */
    public static BigDecimal differenceBetween(final Date earliest, final Date latest, final ChronoUnit unit) {
        if (earliest == null || latest == null || unit == null) {
            throw new IllegalArgumentException("Earliest date, latest date and unit cannot be null");
        }
        final long difference = unit.between(earliest.toLocalDate(), latest.toLocalDate());
        return BigDecimal.valueOf(difference);
    }

    /**
     * Calculates the difference in complete hours between two DateTime objects.
     * <p>
     * This is a convenience method that uses {@link ChronoUnit#HOURS} to calculate
     * the difference. Only complete hours are counted; partial hours are truncated.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Exact hours difference
     * DateTime start = DateTime.of("2024-01-15T10:00:00");
     * DateTime end = DateTime.of("2024-01-15T15:00:00");
     * BigDecimal hours = DateUtils.hoursBetween(start, end);
     * // Returns: 5
     *
     * // Example 2: Hours with partial hour (truncated)
     * DateTime start = DateTime.of("2024-01-15T10:00:00");
     * DateTime end = DateTime.of("2024-01-15T15:30:00");
     * BigDecimal hours = DateUtils.hoursBetween(start, end);
     * // Returns: 5 (partial hour is not counted)
     *
     * // Example 3: Hours across days
     * DateTime start = DateTime.of("2024-01-15T22:00:00");
     * DateTime end = DateTime.of("2024-01-16T02:00:00");
     * BigDecimal hours = DateUtils.hoursBetween(start, end);
     * // Returns: 4
     * }</pre>
     *
     * @param earliest the earlier date and time; must not be null
     * @param latest   the later date and time; must not be null
     * @return the difference between the two dates in complete hours, as a BigDecimal
     * @throws IllegalArgumentException if either of the parameters is null
     * @see #differenceBetween(DateTime, DateTime, ChronoUnit)
     */
    public static BigDecimal hoursBetween(final DateTime earliest, final DateTime latest) {
        return differenceBetween(earliest, latest, ChronoUnit.HOURS);
    }

    /**
     * Calculates the difference in complete days between two DateTime objects.
     * <p>
     * This is a convenience method that uses {@link ChronoUnit#DAYS} to calculate
     * the difference. Only complete 24-hour periods are counted; partial days are truncated.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Exact days difference
     * DateTime start = DateTime.of("2024-01-15T00:00:00");
     * DateTime end = DateTime.of("2024-01-20T00:00:00");
     * BigDecimal days = DateUtils.daysBetween(start, end);
     * // Returns: 5
     *
     * // Example 2: Days with partial day (truncated)
     * DateTime start = DateTime.of("2024-01-15T10:00:00");
     * DateTime end = DateTime.of("2024-01-20T15:00:00");
     * BigDecimal days = DateUtils.daysBetween(start, end);
     * // Returns: 5 (partial day is not counted)
     *
     * // Example 3: One week difference
     * DateTime start = DateTime.of("2024-01-15T00:00:00");
     * DateTime end = DateTime.of("2024-01-22T00:00:00");
     * BigDecimal days = DateUtils.daysBetween(start, end);
     * // Returns: 7
     * }</pre>
     *
     * @param earliest the earlier date and time; must not be null
     * @param latest   the later date and time; must not be null
     * @return the difference between the two dates in complete days, as a BigDecimal
     * @throws IllegalArgumentException if either of the parameters is null
     * @see #differenceBetween(DateTime, DateTime, ChronoUnit)
     */
    public static BigDecimal daysBetween(final DateTime earliest, final DateTime latest) {
        return differenceBetween(earliest, latest, ChronoUnit.DAYS);
    }

    /**
     * Calculates the difference in complete months between two DateTime objects.
     * <p>
     * This is a convenience method that uses {@link ChronoUnit#MONTHS} to calculate
     * the difference. Only complete calendar months are counted; partial months are truncated.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Exact months difference
     * DateTime start = DateTime.of("2024-01-15T00:00:00");
     * DateTime end = DateTime.of("2024-04-15T00:00:00");
     * BigDecimal months = DateUtils.monthsBetween(start, end);
     * // Returns: 3
     *
     * // Example 2: Months with partial month (truncated)
     * DateTime start = DateTime.of("2024-01-15T00:00:00");
     * DateTime end = DateTime.of("2024-04-20T00:00:00");
     * BigDecimal months = DateUtils.monthsBetween(start, end);
     * // Returns: 3 (partial month is not counted)
     *
     * // Example 3: One year difference
     * DateTime start = DateTime.of("2023-06-01T00:00:00");
     * DateTime end = DateTime.of("2024-06-01T00:00:00");
     * BigDecimal months = DateUtils.monthsBetween(start, end);
     * // Returns: 12
     * }</pre>
     *
     * @param earliest the earlier date and time; must not be null
     * @param latest   the later date and time; must not be null
     * @return the difference between the two dates in complete months as a BigDecimal
     * @throws IllegalArgumentException if either of the parameters is null
     * @see #differenceBetween(DateTime, DateTime, ChronoUnit)
     */
    public static BigDecimal monthsBetween(final DateTime earliest, final DateTime latest) {
        return differenceBetween(earliest, latest, ChronoUnit.MONTHS);
    }

    /**
     * Calculates the difference in complete years between two DateTime objects.
     * <p>
     * This is a convenience method that uses {@link ChronoUnit#YEARS} to calculate
     * the difference. Only complete calendar years are counted; partial years are truncated.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Exact years difference
     * DateTime start = DateTime.of("2020-01-15T00:00:00");
     * DateTime end = DateTime.of("2024-01-15T00:00:00");
     * BigDecimal years = DateUtils.yearsBetween(start, end);
     * // Returns: 4
     *
     * // Example 2: Years with partial year (truncated)
     * DateTime start = DateTime.of("2020-01-15T00:00:00");
     * DateTime end = DateTime.of("2024-03-20T00:00:00");
     * BigDecimal years = DateUtils.yearsBetween(start, end);
     * // Returns: 4 (partial year is not counted)
     *
     * // Example 3: One decade difference
     * DateTime start = DateTime.of("2010-06-01T00:00:00");
     * DateTime end = DateTime.of("2020-06-01T00:00:00");
     * BigDecimal years = DateUtils.yearsBetween(start, end);
     * // Returns: 10
     * }</pre>
     *
     * @param earliest the earlier date and time; must not be null
     * @param latest   the later date and time; must not be null
     * @return the difference between the two dates in complete years, as a BigDecimal
     * @throws IllegalArgumentException if either of the parameters is null
     * @see #differenceBetween(DateTime, DateTime, ChronoUnit)
     */
    public static BigDecimal yearsBetween(final DateTime earliest, final DateTime latest) {
        return differenceBetween(earliest, latest, ChronoUnit.YEARS);
    }

    /**
     * Calculates the difference in complete days between two Date objects.
     * <p>
     * This is a convenience method that uses {@link ChronoUnit#DAYS} to calculate
     * the difference. Only complete days are counted.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Exact days difference
     * Date start = Date.of("2024-01-15");
     * Date end = Date.of("2024-01-20");
     * BigDecimal days = DateUtils.daysBetween(start, end);
     * // Returns: 5
     *
     * // Example 2: One week difference
     * Date start = Date.of("2024-01-15");
     * Date end = Date.of("2024-01-22");
     * BigDecimal days = DateUtils.daysBetween(start, end);
     * // Returns: 7
     *
     * // Example 3: Month span
     * Date start = Date.of("2024-01-01");
     * Date end = Date.of("2024-01-31");
     * BigDecimal days = DateUtils.daysBetween(start, end);
     * // Returns: 30
     * }</pre>
     *
     * @param earliest the earlier date; must not be null
     * @param latest   the later date; must not be null
     * @return the difference between the two dates in complete days, as a BigDecimal
     * @throws IllegalArgumentException if either of the parameters is null
     * @see #differenceBetween(Date, Date, ChronoUnit)
     */
    public static BigDecimal daysBetween(final Date earliest, final Date latest) {
        return differenceBetween(earliest, latest, ChronoUnit.DAYS);
    }

    /**
     * Calculates the difference in complete months between two Date objects.
     * <p>
     * This is a convenience method that uses {@link ChronoUnit#MONTHS} to calculate
     * the difference. Only complete calendar months are counted; partial months are truncated.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Exact months difference
     * Date start = Date.of("2024-01-15");
     * Date end = Date.of("2024-04-15");
     * BigDecimal months = DateUtils.monthsBetween(start, end);
     * // Returns: 3
     *
     * // Example 2: Months with partial month (truncated)
     * Date start = Date.of("2024-01-15");
     * Date end = Date.of("2024-04-20");
     * BigDecimal months = DateUtils.monthsBetween(start, end);
     * // Returns: 3 (partial month is not counted)
     *
     * // Example 3: One year difference
     * Date start = Date.of("2023-06-01");
     * Date end = Date.of("2024-06-01");
     * BigDecimal months = DateUtils.monthsBetween(start, end);
     * // Returns: 12
     * }</pre>
     *
     * @param earliest the earlier date; must not be null
     * @param latest   the later date; must not be null
     * @return the difference between the two dates in complete months as a BigDecimal
     * @throws IllegalArgumentException if either of the parameters is null
     * @see #differenceBetween(Date, Date, ChronoUnit)
     */
    public static BigDecimal monthsBetween(final Date earliest, final Date latest) {
        return differenceBetween(earliest, latest, ChronoUnit.MONTHS);
    }

    /**
     * Calculates the difference in complete years between two Date objects.
     * <p>
     * This is a convenience method that uses {@link ChronoUnit#YEARS} to calculate
     * the difference. Only complete calendar years are counted; partial years are truncated.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Exact years difference
     * Date start = Date.of("2020-01-15");
     * Date end = Date.of("2024-01-15");
     * BigDecimal years = DateUtils.yearsBetween(start, end);
     * // Returns: 4
     *
     * // Example 2: Years with partial year (truncated)
     * Date start = Date.of("2020-01-15");
     * Date end = Date.of("2024-03-20");
     * BigDecimal years = DateUtils.yearsBetween(start, end);
     * // Returns: 4 (partial year is not counted)
     *
     * // Example 3: One decade difference
     * Date start = Date.of("2010-06-01");
     * Date end = Date.of("2020-06-01");
     * BigDecimal years = DateUtils.yearsBetween(start, end);
     * // Returns: 10
     * }</pre>
     *
     * @param earliest the earlier date; must not be null
     * @param latest   the later date; must not be null
     * @return the difference between the two dates in complete years, as a BigDecimal
     * @throws IllegalArgumentException if either of the parameters is null
     * @see #differenceBetween(Date, Date, ChronoUnit)
     */
    public static BigDecimal yearsBetween(final Date earliest, final Date latest) {
        return differenceBetween(earliest, latest, ChronoUnit.YEARS);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Calculates an expiration date by adding expiration time and contingency time to a base date.
     * <p>
     * This method adds both the expiration seconds and additional contingency seconds to the base date.
     * Negative values are treated as zero. This is useful for calculating expiration times with
     * a safety buffer.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Calculate expiration with 1 hour and 5 minutes buffer
     * DateTime baseDate = DateTime.of("2024-01-15T10:00:00");
     * DateTime expirationDate = DateUtils.calculateExpired(baseDate, 3600L, 300L);
     * // Returns: DateTime representing 2024-01-15T11:05:00 (1 hour + 5 minutes later)
     *
     * // Example 2: Calculate expiration with only expire time (no contingency)
     * DateTime baseDate = DateTime.of("2024-01-15T10:00:00");
     * DateTime expirationDate = DateUtils.calculateExpired(baseDate, 7200L, 0L);
     * // Returns: DateTime representing 2024-01-15T12:00:00 (2 hours later)
     *
     * // Example 3: Negative values treated as zero
     * DateTime baseDate = DateTime.of("2024-01-15T10:00:00");
     * DateTime expirationDate = DateUtils.calculateExpired(baseDate, -100L, -50L);
     * // Returns: DateTime representing 2024-01-15T10:00:00 (unchanged)
     * }</pre>
     *
     * @param date               the base date to calculate from; must not be null
     * @param expireInSeconds    seconds until expiration (negative values treated as zero)
     * @param contingencySeconds additional contingency/buffer seconds (negative values treated as zero)
     * @return calculated expiration DateTime
     * @throws IllegalArgumentException if date is null
     */
    public static DateTime calculateExpired(final DateTime date, final long expireInSeconds, final long contingencySeconds) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        final long totalAdditionalSeconds = Math.max(0L, expireInSeconds) + Math.max(0L, contingencySeconds);
        return date.plusSeconds(totalAdditionalSeconds);
    }

    /**
     * Calculates an expiration date by adding the specified number of seconds to the base date.
     * <p>
     * This is a convenience method that calls {@link #calculateExpired(DateTime, long, long)}
     * with zero contingency seconds. Negative values are treated as zero.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Calculate expiration in 1 hour
     * DateTime baseDate = DateTime.of("2024-01-15T10:00:00");
     * DateTime expirationDate = DateUtils.calculateExpired(baseDate, 3600L);
     * // Returns: DateTime representing 2024-01-15T11:00:00
     *
     * // Example 2: Calculate expiration in 30 minutes
     * DateTime baseDate = DateTime.of("2024-01-15T14:00:00");
     * DateTime expirationDate = DateUtils.calculateExpired(baseDate, 1800L);
     * // Returns: DateTime representing 2024-01-15T14:30:00
     *
     * // Example 3: Calculate expiration in 24 hours
     * DateTime baseDate = DateTime.of("2024-01-15T00:00:00");
     * DateTime expirationDate = DateUtils.calculateExpired(baseDate, 86400L);
     * // Returns: DateTime representing 2024-01-16T00:00:00
     * }</pre>
     *
     * @param date      the base date to calculate expiration from; must not be null
     * @param expireIn  the number of seconds until expiration (negative values treated as zero)
     * @return the calculated expiration DateTime
     * @throws IllegalArgumentException if the date is null
     * @see #calculateExpired(DateTime, long, long)
     */
    public static DateTime calculateExpired(final DateTime date, final long expireIn){
        return calculateExpired(date, expireIn, 0);
    }

    /**
     * Calculates an expiration date by adding expiration time and contingency time to a base date.
     * <p>
     * This method adds both the expiration seconds and additional contingency seconds to the base date.
     * Negative values are treated as zero. This is useful for calculating expiration dates with
     * a safety buffer. Days are calculated as 86400 seconds per day.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Calculate expiration with 1 day and 1 day buffer
     * Date baseDate = Date.of("2024-01-15");
     * Date expirationDate = DateUtils.calculateExpired(baseDate, 86400L, 86400L);
     * // Returns: Date representing 2024-01-17 (2 days later)
     *
     * // Example 2: Calculate expiration with only expire time (no contingency)
     * Date baseDate = Date.of("2024-01-15");
     * Date expirationDate = DateUtils.calculateExpired(baseDate, 172800L, 0L);
     * // Returns: Date representing 2024-01-17 (2 days later)
     *
     * // Example 3: Negative values treated as zero
     * Date baseDate = Date.of("2024-01-15");
     * Date expirationDate = DateUtils.calculateExpired(baseDate, -100L, -50L);
     * // Returns: Date representing 2024-01-15 (unchanged)
     * }</pre>
     *
     * @param date               the base date to calculate from; must not be null
     * @param expireInSeconds    seconds until expiration (negative values treated as zero)
     * @param contingencySeconds additional contingency/buffer seconds (negative values treated as zero)
     * @return calculated expiration Date
     * @throws IllegalArgumentException if date is null
     */
    public static Date calculateExpired(final Date date, final long expireInSeconds, final long contingencySeconds) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        final long totalAdditionalSeconds = Math.max(0L, expireInSeconds) + Math.max(0L, contingencySeconds);
        final long totalDays = totalAdditionalSeconds / 86400;
        return date.plusDays((int) totalDays);
    }

    /**
     * Calculates an expiration date by adding the specified number of seconds to the base date.
     * <p>
     * This is a convenience method that calls {@link #calculateExpired(Date, long, long)}
     * with zero contingency seconds. Negative values are treated as zero.
     * Days are calculated as 86400 seconds per day.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Calculate expiration in 1 day
     * Date baseDate = Date.of("2024-01-15");
     * Date expirationDate = DateUtils.calculateExpired(baseDate, 86400L);
     * // Returns: Date representing 2024-01-16
     *
     * // Example 2: Calculate expiration in 3 days
     * Date baseDate = Date.of("2024-01-15");
     * Date expirationDate = DateUtils.calculateExpired(baseDate, 259200L);
     * // Returns: Date representing 2024-01-18
     *
     * // Example 3: Calculate expiration in 7 days
     * Date baseDate = Date.of("2024-01-15");
     * Date expirationDate = DateUtils.calculateExpired(baseDate, 604800L);
     * // Returns: Date representing 2024-01-22
     * }</pre>
     *
     * @param date     the base date to calculate expiration from; must not be null
     * @param expireIn the number of seconds until expiration (negative values treated as zero)
     * @return the calculated expiration Date
     * @throws IllegalArgumentException if the date is null
     * @see #calculateExpired(Date, long, long)
     */
    public static Date calculateExpired(final Date date, final long expireIn) {
        return calculateExpired(date, expireIn, 0);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Checks if a given DateTime has expired by comparing it to an expiration DateTime.
     * <p>
     * This method returns true if the date to check is after (later than) the expiration date,
     * indicating that it has expired. Both dates are converted to Instant for accurate comparison.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Check if current date exceeds expiration
     * DateTime current = DateTime.of("2024-01-15T15:00:00");
     * DateTime expiration = DateTime.of("2024-01-15T14:00:00");
     * boolean expired = DateUtils.isExpired(current, expiration);
     * // Returns: true (current is after expiration)
     *
     * // Example 2: Check if current date is before expiration
     * DateTime current = DateTime.of("2024-01-15T13:00:00");
     * DateTime expiration = DateTime.of("2024-01-15T14:00:00");
     * boolean expired = DateUtils.isExpired(current, expiration);
     * // Returns: false (current is before expiration)
     *
     * // Example 3: Check exact expiration time
     * DateTime current = DateTime.of("2024-01-15T14:00:00");
     * DateTime expiration = DateTime.of("2024-01-15T14:00:00");
     * boolean expired = DateUtils.isExpired(current, expiration);
     * // Returns: false (same instant is not considered expired)
     * }</pre>
     *
     * @param dateToCheck the date to check for expiration; must not be null
     * @param expireDate  the expiration date to compare against; must not be null
     * @return true if dateToCheck is after expireDate, false otherwise
     * @throws IllegalArgumentException if any parameter is null
     */
    public static boolean isExpired(final DateTime dateToCheck, final DateTime expireDate) {
        if (dateToCheck == null || expireDate == null) {
            throw new IllegalArgumentException("Date to check and expire date cannot be null");
        }
        return dateToCheck.toInstant().isAfter(expireDate.toInstant());
    }
    
    /**
     * Checks if a given DateTime has expired by comparing it to the current date and time.
     * <p>
     * This is a convenience method that checks if the provided date is after the current moment.
     * It internally calls {@link #isExpired(DateTime, DateTime)} using {@link DateTime#now()} as
     * the expiration date.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Check if past date is expired
     * DateTime pastDate = DateTime.of("2023-01-15T10:00:00");
     * boolean expired = DateUtils.isExpired(pastDate);
     * // Returns: false (past date is before current time)
     *
     * // Example 2: Check if future date is expired
     * DateTime futureDate = DateTime.of("2025-01-15T10:00:00");
     * boolean expired = DateUtils.isExpired(futureDate);
     * // Returns: true (assuming current time is before 2025-01-15)
     *
     * // Example 3: Check if timestamp from 1 hour ago is expired
     * DateTime oneHourAgo = DateTime.now().minusSeconds(3600);
     * boolean expired = DateUtils.isExpired(oneHourAgo);
     * // Returns: false (one hour ago is before now)
     * }</pre>
     *
     * @param dateToCheck the date to check for expiration; must not be null
     * @return true if the dateToCheck is after the current date and time, false otherwise
     * @throws IllegalArgumentException if the dateToCheck is null
     * @see #isExpired(DateTime, DateTime)
     * @see DateTime#now()
     */
    public static boolean isExpired(final DateTime dateToCheck){
        return isExpired(dateToCheck, DateTime.now());
    }

    /**
     * Checks if a given Date has expired by comparing it to an expiration Date.
     * <p>
     * This method returns true if the date to check is after (later than) the expiration date,
     * indicating that it has expired. Both dates are converted to LocalDate for day-level comparison,
     * ignoring time components.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Check if current date exceeds expiration
     * Date current = Date.of("2024-01-15");
     * Date expiration = Date.of("2024-01-10");
     * boolean expired = DateUtils.isExpired(current, expiration);
     * // Returns: true (current is after expiration)
     *
     * // Example 2: Check if current date is before expiration
     * Date current = Date.of("2024-01-10");
     * Date expiration = Date.of("2024-01-15");
     * boolean expired = DateUtils.isExpired(current, expiration);
     * // Returns: false (current is before expiration)
     *
     * // Example 3: Check same date
     * Date current = Date.of("2024-01-15");
     * Date expiration = Date.of("2024-01-15");
     * boolean expired = DateUtils.isExpired(current, expiration);
     * // Returns: false (same date is not considered expired)
     * }</pre>
     *
     * @param dateToCheck the date to check for expiration; must not be null
     * @param expireDate  the expiration date to compare against; must not be null
     * @return true if dateToCheck is after expireDate, false otherwise
     * @throws IllegalArgumentException if any parameter is null
     */
    public static boolean isExpired(final Date dateToCheck, final Date expireDate) {
        if (dateToCheck == null || expireDate == null) {
            throw new IllegalArgumentException("Date to check and expire date cannot be null");
        }
        return dateToCheck.toLocalDate().isAfter(expireDate.toLocalDate());
    }


    /**
     * Checks if a given Date has expired by comparing it to today's date.
     * <p>
     * This is a convenience method that checks if the provided date is after today.
     * It internally calls {@link #isExpired(Date, Date)} using {@link Date#today()} as
     * the expiration date. Time components are not considered in this comparison.
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <pre>{@code
     * // Example 1: Check if past date is expired
     * Date pastDate = Date.of("2023-01-15");
     * boolean expired = DateUtils.isExpired(pastDate);
     * // Returns: false (assuming today is after 2023-01-15)
     *
     * // Example 2: Check if future date is expired
     * Date futureDate = Date.of("2025-01-15");
     * boolean expired = DateUtils.isExpired(futureDate);
     * // Returns: true (assuming today is before 2025-01-15)
     *
     * // Example 3: Check today's date
     * Date today = Date.today();
     * boolean expired = DateUtils.isExpired(today);
     * // Returns: false (today is not after today)
     * }</pre>
     *
     * @param dateToCheck the date to check for expiration; must not be null
     * @return true if the dateToCheck is after today's date, false otherwise
     * @throws IllegalArgumentException if the dateToCheck is null
     * @see #isExpired(Date, Date)
     * @see Date#today()
     */
    public static boolean isExpired(final Date dateToCheck){
        return isExpired(dateToCheck, Date.today());
    }

}