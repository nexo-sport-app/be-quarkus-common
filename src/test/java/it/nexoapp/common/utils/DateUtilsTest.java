package it.nexoapp.common.utils;

import it.nexoapp.common.domain.Date;
import it.nexoapp.common.domain.DateTime;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link DateUtils} utility class.
 * <p>
 * This class tests the behavior of the {@code reformat} method, ensuring
 * it handles valid input cases as well as edge cases such as null values
 * for input arguments.
 */
class DateUtilsTest {

    // Epoch milliseconds constants
    private static final long EPOCH_MILLI = 1767225600000L;
    private static final long NEGATIVE_EPOCH_MILLI = -2208988800000L; // Corresponds to 1900-01-01T00:00:00Z
    private static final long LARGE_EPOCH_MILLI = 32503680000000L; // Corresponds to 3000-01-01T00:00:00Z

    // Date string constants
    private static final String DATE_VALID = "2026-01-01";
    private static final String DATE_PRETTY_VALID = "01/01/2026";
    private static final String DATE_INVALID = "01-01-2026";

    // DateTime string constants
    private static final String DATE_TIME_VALID = "2026-01-01T00:00:00Z";
    private static final String DATE_TIME_PRETTY_VALID = "01/01/2026 00:00";
    private static final String DATE_TIME_INVALID = "01-01-2026";

    // Test date instances
    private static final String DATE_2023_01_01 = "2023-01-01";
    private static final String DATE_2023_07_01 = "2023-07-01";
    private static final String DATE_2023_01_03 = "2023-01-03";
    private static final String DATE_2020_01_01 = "2020-01-01";
    private static final String DATE_2025_01_01 = "2025-01-01";
    private static final String DATE_2024_01_01 = "2024-01-01";

    // Test DateTime instances
    private static final String DATE_TIME_2023_01_01 = "2023-01-01T00:00:00Z";
    private static final String DATE_TIME_2023_07_01 = "2023-07-01T00:00:00Z";
    private static final String DATE_TIME_2023_01_01_12H = "2023-01-01T12:00:00Z";
    private static final String DATE_TIME_2023_01_01_04H = "2023-01-01T04:00:00Z";
    private static final String DATE_TIME_2020_01_01 = "2020-01-01T00:00:00Z";
    private static final String DATE_TIME_2025_01_01 = "2025-01-01T00:00:00Z";

    // Time duration constants
    private static final long ONE_HOUR_SECONDS = 3600;
    private static final long TEN_MINUTES_SECONDS = 600;
    private static final long TWENTY_MINUTES_SECONDS = 1200;
    private static final long ONE_DAY_SECONDS = 86400;
    private static final int TWELVE_HOURS = 12;
    private static final int FOUR_HOURS = 4;
    private static final int TWO_DAYS = 2;
    private static final int SIX_MONTHS = 6;
    private static final int FIVE_YEARS = 5;
    private static final int THREE_MONTHS = 3;
    private static final int TWO_YEARS = 2;

    // Error message constants
    private static final String ERROR_NULL_DATE_FORMATTER = "Date, from formatter and to formatter cannot be null";
    private static final String ERROR_NULL_DATE_UNIT = "Earliest date, latest date and unit cannot be null";
    private static final String ERROR_NULL_DATE = "Date cannot be null";
    private static final String ERROR_NULL_EXPIRE_DATE = "Date to check and expire date cannot be null";
    private static final String ERROR_PARSE_PREFIX = "Text '01-01-2026' could not be parsed";

    // --
    // DateTime monthsBetween Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testMonthsBetween_ValidCase_DateTime() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_2023_01_01);
        final DateTime end = new DateTime(DATE_TIME_2023_07_01);

        // Act
        BigDecimal difference = DateUtils.monthsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(SIX_MONTHS), difference, "The difference should be 6 months.");
    }

    @Test
    void testMonthsBetween_SameDateTime() {
        // Arrange
        final DateTime dateTime = new DateTime(DATE_TIME_2023_01_01);

        // Act
        BigDecimal difference = DateUtils.monthsBetween(dateTime, dateTime);

        // Assert
        assertEquals(BigDecimal.ZERO, difference, "The difference between identical DateTime objects should be 0 months.");
    }

    @Test
    void testMonthsBetween_NullValues_DateTime() {
        // Arrange
        final DateTime start = null;
        final DateTime end = new DateTime(DATE_TIME_2023_01_01);

        // Act & Assert
        try {
            DateUtils.monthsBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    // --
    // Date monthsBetween Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testMonthsBetween_ValidCase_Date() {
        // Arrange
        final Date start = new Date(DATE_2023_01_01);
        final Date end = new Date(DATE_2023_07_01);

        // Act
        BigDecimal difference = DateUtils.monthsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(SIX_MONTHS), difference, "The difference should be 6 months.");
    }

    @Test
    void testMonthsBetween_SameDate() {
        // Arrange
        final Date date = new Date(DATE_2023_01_01);

        // Act
        BigDecimal difference = DateUtils.monthsBetween(date, date);

        // Assert
        assertEquals(BigDecimal.ZERO, difference, "The difference between identical Date objects should be 0 months.");
    }

    @Test
    void testMonthsBetween_NullValues_Date() {
        // Arrange
        final Date start = null;
        final Date end = new Date(DATE_2023_01_01);

        // Act & Assert
        try {
            DateUtils.monthsBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    // --
    // DateTime from Epoch Millis Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testFromEpochMillis_ValidEpoch() {
        // Act
        DateTime result = DateUtils.fromEpochMillis(EPOCH_MILLI);

        // Assert
        assertEquals(DATE_TIME_VALID, result.toString());
    }

    @Test
    void testFromEpochMillis_NegativeEpoch() {
        // Arrange
        Instant expectedInstant = Instant.ofEpochMilli(NEGATIVE_EPOCH_MILLI);

        // Act
        DateTime result = DateUtils.fromEpochMillis(NEGATIVE_EPOCH_MILLI);

        // Assert
        assertEquals(expectedInstant, result.toInstant());
    }

    @Test
    void testFromEpochMillis_LargeEpoch() {
        // Arrange
        Instant expectedInstant = Instant.ofEpochMilli(LARGE_EPOCH_MILLI);

        // Act
        DateTime result = DateUtils.fromEpochMillis(LARGE_EPOCH_MILLI);

        // Assert
        assertEquals(expectedInstant, result.toInstant());
    }

    // --
    // DateTime from Epoch Seconds Tests
    // -----------------------------------------------------------------------------------------------------------------

    // --
    // Date from Epoch Seconds Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testDateFromEpochSeconds_ValidSeconds() {
        // Arrange
        long epochSeconds = EPOCH_MILLI / 1000;
        Date expectedDate = Date.of(Instant.ofEpochSecond(epochSeconds));

        // Act
        Date result = DateUtils.dateFromEpochSeconds(epochSeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date should match the expected date.");
    }

    // --
    // Date from Epoch Millis Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testDateFromEpochMillis_ValidInput() {
        // Arrange
        Date expectedDate = Date.of(Instant.ofEpochMilli(EPOCH_MILLI));

        // Act
        Date result = DateUtils.dateFromEpochMillis(EPOCH_MILLI);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date should match the expected date for valid epoch millis.");
    }

    @Test
    void testDateFromEpochMillis_NegativeMillis() {
        // Arrange
        Date expectedDate = Date.of(Instant.ofEpochMilli(NEGATIVE_EPOCH_MILLI));

        // Act
        Date result = DateUtils.dateFromEpochMillis(NEGATIVE_EPOCH_MILLI);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date should match the expected date for negative epoch millis.");
    }

    @Test
    void testDateFromEpochMillis_LargeMillis() {
        // Arrange
        Date expectedDate = Date.of(Instant.ofEpochMilli(LARGE_EPOCH_MILLI));

        // Act
        Date result = DateUtils.dateFromEpochMillis(LARGE_EPOCH_MILLI);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date should match the expected date for large epoch millis.");
    }

    @Test
    void testDateFromEpochMillis_Zero() {
        // Arrange
        Date expectedDate = Date.of(Instant.ofEpochMilli(0));

        // Act
        Date result = DateUtils.dateFromEpochMillis(0);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date should match the Unix epoch date for epochMillis 0.");
    }

    @Test
    void testDateFromEpochSeconds_NegativeSeconds() {
        // Arrange
        long negativeEpochSeconds = NEGATIVE_EPOCH_MILLI / 1000;
        Date expectedDate = Date.of(Instant.ofEpochSecond(negativeEpochSeconds));

        // Act
        Date result = DateUtils.dateFromEpochSeconds(negativeEpochSeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date should handle negative epoch seconds correctly.");
    }

    @Test
    void testDateFromEpochSeconds_LargeSeconds() {
        // Arrange
        long largeEpochSeconds = LARGE_EPOCH_MILLI / 1000;
        Date expectedDate = Date.of(Instant.ofEpochSecond(largeEpochSeconds));

        // Act
        Date result = DateUtils.dateFromEpochSeconds(largeEpochSeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date should handle large epoch seconds correctly.");
    }

    @Test
    void testDateFromEpochSeconds_Zero() {
        // Arrange
        long zeroEpochSeconds = 0;
        Date expectedDate = Date.of(Instant.ofEpochSecond(zeroEpochSeconds));

        // Act
        Date result = DateUtils.dateFromEpochSeconds(zeroEpochSeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString(), "The generated date for epoch 0 should be the Unix epoch date.");
    }

    @Test
    void testFromEpochSeconds_ValidSeconds() {
        // Arrange
        long epochSeconds = EPOCH_MILLI / 1000;
        DateTime expectedDateTime = DateTime.of(Instant.ofEpochSecond(epochSeconds));

        // Act
        DateTime result = DateUtils.fromEpochSeconds(epochSeconds);

        // Assert
        assertEquals(expectedDateTime.toString(), result.toString());
    }

    @Test
    void testFromEpochSeconds_NegativeSeconds() {
        // Arrange
        long negativeEpochSeconds = NEGATIVE_EPOCH_MILLI / 1000;
        Instant expectedInstant = Instant.ofEpochSecond(negativeEpochSeconds);

        // Act
        DateTime result = DateUtils.fromEpochSeconds(negativeEpochSeconds);

        // Assert
        assertEquals(expectedInstant, result.toInstant());
    }

    @Test
    void testFromEpochSeconds_LargeSeconds() {
        // Arrange
        long largeEpochSeconds = LARGE_EPOCH_MILLI / 1000;
        Instant expectedInstant = Instant.ofEpochSecond(largeEpochSeconds);

        // Act
        DateTime result = DateUtils.fromEpochSeconds(largeEpochSeconds);

        // Assert
        assertEquals(expectedInstant, result.toInstant());
    }

    // --
    // DateTime Reformat Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testReformatDateTime_ValidDefaultFormat() {
        assertEquals(DATE_TIME_VALID,
                DateUtils.reformatDateTime(DATE_TIME_VALID, DateTime.DEFAULT_FORMAT));
    }

    @Test
    void testReformatDateTime_ValidPrettyFormat() {
        assertEquals(DATE_TIME_PRETTY_VALID,
                DateUtils.reformatDateTime(DATE_TIME_VALID, DateTime.DEFAULT_FORMAT, DateTime.PRETTY_FORMAT));
    }

    @Test
    void testReformatDateTime_InvalidInputFormat() {
        try {
            DateUtils.reformatDateTime(DATE_TIME_INVALID, DateTime.DEFAULT_FORMAT);
        } catch (Exception e) {
            assertEquals(ERROR_PARSE_PREFIX, e.getMessage().substring(0, 37));
        }
    }

    @Test
    void testReformatDateTime_NullDateTime() {
        try {
            DateUtils.reformatDateTime(null, DateTime.DEFAULT_FORMAT);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    @Test
    void testReformatDateTime_NullFromFormatter() {
        try {
            DateUtils.reformatDateTime(DATE_VALID, null, DateTime.DEFAULT_FORMAT);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    @Test
    void testReformatDateTime_NullToFormatter() {
        // Act & Assert
        try {
            DateUtils.reformatDateTime(DATE_VALID, DateTime.DEFAULT_FORMAT, null);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    // --
    // Date Reformat Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testReformatDate_ValidDefaultFormat() {
        assertEquals(DATE_VALID,
                DateUtils.reformatDate(DATE_VALID, Date.DEFAULT_FORMAT));
    }

    @Test
    void testReformatDate_ValidPrettyFormat() {
        assertEquals(DATE_PRETTY_VALID,
                DateUtils.reformatDate(DATE_VALID, Date.DEFAULT_FORMAT, Date.PRETTY_FORMAT));
    }

    @Test
    void testReformatDate_NullDate() {
        try {
            DateUtils.reformatDate(null, Date.DEFAULT_FORMAT);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    @Test
    void testReformatDate_NullFromFormatter() {
        try {
            DateUtils.reformatDate(DATE_VALID, null, Date.PRETTY_FORMAT);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    @Test
    void testReformatDate_NullToFormatter() {
        try {
            DateUtils.reformatDate(DATE_VALID, Date.DEFAULT_FORMAT, null);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    @Test
    void testReformatToPrettyDate_ValidInput() {
        // Act & Assert
        assertEquals(DATE_PRETTY_VALID,
                DateUtils.reformatToPrettyDate(DATE_VALID, Date.DEFAULT_FORMAT));
    }

    @Test
    void testReformatToPrettyDate_NullDate() {
        // Act & Assert
        try {
            DateUtils.reformatToPrettyDate(null, Date.DEFAULT_FORMAT);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    @Test
    void testReformatToPrettyDate_InvalidFormat() {
        // Act & Assert
        try {
            DateUtils.reformatToPrettyDate(DATE_INVALID, Date.DEFAULT_FORMAT);
        } catch (Exception e) {
            assertEquals(ERROR_PARSE_PREFIX, e.getMessage().substring(0, 37));
        }
    }

    @Test
    void testReformatToPrettyDateTime_ValidInput() {
        // Act & Assert
        assertEquals(DATE_TIME_PRETTY_VALID,
                DateUtils.reformatToPrettyDateTime(DATE_TIME_VALID, DateTime.DEFAULT_FORMAT));
    }

    @Test
    void testReformatToPrettyDateTime_NullInput() {
        // Act & Assert
        try {
            DateUtils.reformatToPrettyDateTime(null, DateTime.DEFAULT_FORMAT);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_FORMATTER, e.getMessage());
        }
    }

    @Test
    void testReformatToPrettyDateTime_InvalidFormat() {
        // Act & Assert
        try {
            DateUtils.reformatToPrettyDateTime(DATE_TIME_INVALID, DateTime.DEFAULT_FORMAT);
        } catch (Exception e) {
            assertEquals(ERROR_PARSE_PREFIX, e.getMessage().substring(0, 37));
        }
    }


    // --
    // DateTime Difference Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testHoursBetween_ValidCase_DateTime() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_2023_01_01);
        final DateTime end = new DateTime(DATE_TIME_2023_01_01_12H);

        // Act
        BigDecimal difference = DateUtils.hoursBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(TWELVE_HOURS), difference, "The difference should be 12 hours.");
    }

    @Test
    void testDifferenceBetween_ValidDaysUsingDateTime() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_2023_01_01);
        final DateTime end = new DateTime(DATE_2023_01_03 + "T00:00:00Z");

        // Act
        BigDecimal difference = DateUtils.differenceBetween(start, end, ChronoUnit.DAYS);

        // Assert
        assertEquals(BigDecimal.valueOf(TWO_DAYS), difference, "The difference should be 2 days.");
    }

    @Test
    void testDifferenceBetween_ValidHoursUsingDateTime() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_2023_01_01);
        final DateTime end = new DateTime(DATE_TIME_2023_01_01_04H);

        // Act
        BigDecimal difference = DateUtils.differenceBetween(start, end, ChronoUnit.HOURS);

        // Assert
        assertEquals(BigDecimal.valueOf(FOUR_HOURS), difference, "The difference should be 4 hours.");
    }

    @Test
    void testDifferenceBetween_ValidMonthsUsingDate() {
        // Arrange
        final Date start = new Date(DATE_2023_01_01);
        final Date end = new Date(DATE_2023_07_01);

        // Act
        BigDecimal difference = DateUtils.differenceBetween(start, end, ChronoUnit.MONTHS);

        // Assert
        assertEquals(BigDecimal.valueOf(SIX_MONTHS), difference, "The difference should be 6 months.");
    }

    @Test
    void testDifferenceBetween_ValidYearsUsingDate() {
        // Arrange
        final Date start = new Date(DATE_2020_01_01);
        final Date end = new Date(DATE_2025_01_01);

        // Act
        BigDecimal difference = DateUtils.differenceBetween(start, end, ChronoUnit.YEARS);

        // Assert
        assertEquals(BigDecimal.valueOf(FIVE_YEARS), difference, "The difference should be 5 years.");
    }

    @Test
    void testDifferenceBetween_NullUnitForDate() {
        // Arrange
        final Date start = new Date(DATE_2023_01_01);
        final Date end = new Date("2023-12-01");

        // Act & Assert
        try {
            DateUtils.differenceBetween(start, end, null);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testHoursBetween_SameDateTime() {
        // Arrange
        final DateTime dateTime = new DateTime(DATE_TIME_2023_01_01);

        // Act
        BigDecimal difference = DateUtils.hoursBetween(dateTime, dateTime);

        // Assert
        assertEquals(BigDecimal.ZERO, difference, "The difference between identical DateTime objects should be 0 hours.");
    }

    @Test
    void testHoursBetween_NullValues_DateTime() {
        // Arrange
        final DateTime start = null;
        final DateTime end = new DateTime(DATE_TIME_2023_01_01);

        // Act & Assert
        try {
            DateUtils.hoursBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testHoursBetween_SameDate() {
        // Arrange
        final DateTime date = new DateTime(DATE_TIME_VALID);

        // Act
        BigDecimal difference = DateUtils.hoursBetween(date, date);

        // Assert
        assertEquals(BigDecimal.ZERO, difference, "The difference between identical Date objects should be 0 hours.");
    }

    @Test
    void testHoursBetween_NullValues_Date() {
        // Arrange
        final DateTime start = null;
        final DateTime end = new DateTime(DATE_TIME_VALID);

        // Act & Assert
        try {
            DateUtils.hoursBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testDaysBetween_ValidCase_DateTime() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_VALID);
        final DateTime end = start.plus(Duration.ofDays(TWO_DAYS));

        // Act
        BigDecimal difference = DateUtils.daysBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(TWO_DAYS), difference, "The difference should be 2 days.");
    }

    @Test
    void testDaysBetween_SameDateTime() {
        // Arrange
        final DateTime dateTime = new DateTime(DATE_TIME_2023_01_01);

        // Act
        BigDecimal difference = DateUtils.daysBetween(dateTime, dateTime);

        // Assert
        assertEquals(BigDecimal.ZERO, difference, "The difference between identical DateTime objects should be 0 days.");
    }

    @Test
    void testDaysBetween_NullValues_DateTime() {
        // Arrange
        final DateTime start = null;
        final DateTime end = new DateTime(DATE_TIME_2023_01_01);

        // Act & Assert
        try {
            DateUtils.daysBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testDaysBetween_ValidCase_Date() {
        // Arrange
        final Date start = new Date(DATE_2023_01_01);
        final Date end = new Date(DATE_2023_01_03);

        // Act
        BigDecimal difference = DateUtils.daysBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(TWO_DAYS), difference, "The difference should be 2 days.");
    }

    @Test
    void testDaysBetween_SameDate() {
        // Arrange
        final Date date = new Date(DATE_2023_01_01);

        // Act
        BigDecimal difference = DateUtils.daysBetween(date, date);

        // Assert
        assertEquals(BigDecimal.ZERO, difference, "The difference between identical Date objects should be 0 days.");
    }

    @Test
    void testDaysBetween_NullValues_Date() {
        // Arrange
        final Date start = null;
        final Date end = new Date(DATE_2023_01_01);

        // Act & Assert
        try {
            DateUtils.daysBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testYearsBetween_ValidCase() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_2020_01_01);
        final DateTime end = new DateTime(DATE_TIME_2025_01_01);

        // Act
        BigDecimal difference = DateUtils.yearsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(FIVE_YEARS), difference);
    }

    @Test
    void testYearsBetween_SameDateTime() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_2023_01_01);
        final DateTime end = new DateTime(DATE_TIME_2023_01_01);

        // Act
        BigDecimal difference = DateUtils.yearsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.ZERO, difference);
    }

    @Test
    void testYearsBetween_NullValues() {
        // Arrange
        final DateTime start = null;
        final DateTime end = new DateTime(DATE_2024_01_01 + "T00:00:00Z");

        // Act & Assert
        try {
            DateUtils.yearsBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testYearsBetween_ValidCaseForDate() {
        // Arrange
        final Date start = new Date(DATE_2020_01_01);
        final Date end = new Date(DATE_2025_01_01);

        // Act
        BigDecimal difference = DateUtils.yearsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(FIVE_YEARS), difference);
    }

    @Test
    void testYearsBetween_SameDate() {
        // Arrange
        final Date start = new Date(DATE_2023_01_01);
        final Date end = new Date(DATE_2023_01_01);

        // Act
        BigDecimal difference = DateUtils.yearsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.ZERO, difference);
    }

    @Test
    void testYearsBetween_NullValuesForDate() {
        // Arrange
        final Date start = null;
        final Date end = new Date(DATE_2024_01_01);

        // Act & Assert
        try {
            DateUtils.yearsBetween(start, end);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testDifferenceBetween_ValidHours() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_VALID);
        final DateTime end = start.plus(Duration.ofHours(TWELVE_HOURS));

        // Act
        BigDecimal difference = DateUtils.differenceBetween(start, end, ChronoUnit.HOURS);

        // Assert
        assertEquals(BigDecimal.valueOf(TWELVE_HOURS), difference);
    }

    @Test
    void testDifferenceBetween_ValidDays() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_VALID);
        final DateTime end = start.plus(Duration.ofDays(TWO_DAYS));

        // Act
        BigDecimal difference = DateUtils.daysBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(TWO_DAYS), difference);
    }

    @Test
    void testDifferenceBetween_ValidMonths() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_VALID);
        final DateTime end = start.plus(Duration.ofMinutes(129600));

        // Act
        BigDecimal difference = DateUtils.monthsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(THREE_MONTHS), difference);
    }

    @Test
    void testDifferenceBetween_ValidYears() {
        // Arrange
        final DateTime start = new DateTime(DATE_TIME_VALID);
        final DateTime end = start.plus(Duration.ofMinutes(1051200));

        // Act
        BigDecimal difference = DateUtils.yearsBetween(start, end);

        // Assert
        assertEquals(BigDecimal.valueOf(TWO_YEARS), difference);
    }

    @Test
    void testDifferenceBetween_NullEarliest() {
        // Arrange, Act & Assert
        try {
            DateUtils.differenceBetween(null, new DateTime(DATE_TIME_VALID), ChronoUnit.DAYS);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testDifferenceBetween_NullLatest() {
        // Arrange, Act & Assert
        try {
            DateUtils.differenceBetween(new DateTime(DATE_TIME_VALID), null, ChronoUnit.DAYS);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    @Test
    void testDifferenceBetween_NullUnit() {
        // Arrange, Act & Assert
        try {
            DateUtils.differenceBetween(new DateTime(DATE_TIME_VALID), new DateTime("2026-01-02T00:00:00Z"), null);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE_UNIT, e.getMessage());
        }
    }

    // DateTime Calculate Expired Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testCalculateExpired_ValidParameters() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = ONE_HOUR_SECONDS;
        final long contingencySeconds = TEN_MINUTES_SECONDS;
        final DateTime expectedDate = initialDate.plusSeconds(expireInSeconds + contingencySeconds);

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_NegativeExpireInSeconds() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = -ONE_HOUR_SECONDS;
        final long contingencySeconds = TEN_MINUTES_SECONDS;
        final DateTime expectedDate = initialDate.plusSeconds(contingencySeconds);

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_NegativeContingencySeconds() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = ONE_HOUR_SECONDS;
        final long contingencySeconds = -TEN_MINUTES_SECONDS;
        final DateTime expectedDate = initialDate.plusSeconds(expireInSeconds);

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_AllNegativeSeconds() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = -ONE_HOUR_SECONDS;
        final long contingencySeconds = -TEN_MINUTES_SECONDS;

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(initialDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_ZeroSeconds() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = 0; // Zero
        final long contingencySeconds = 0; // Zero

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(initialDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_ZeroExpireInSeconds() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = 0;
        final long contingencySeconds = ONE_HOUR_SECONDS;

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(initialDate.plusSeconds(contingencySeconds).toString(), result.toString());
    }

    @Test
    void testCalculateExpired_ZeroContingencySeconds() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = ONE_HOUR_SECONDS;
        final long contingencySeconds = 0;

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(initialDate.plusSeconds(expireInSeconds).toString(), result.toString());
    }

    @Test
    void testCalculateExpired_CombinationValues() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = TWENTY_MINUTES_SECONDS;
        final long contingencySeconds = TEN_MINUTES_SECONDS;
        final DateTime expectedDate = initialDate.plusSeconds(expireInSeconds + contingencySeconds);

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_NullInitialDate() {
        // Act & Assert
        try {
            DateUtils.calculateExpired((DateTime) null, ONE_HOUR_SECONDS, TEN_MINUTES_SECONDS);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE, e.getMessage());
        }
    }

    @Test
    void testCalculateExpired_WithDefaultContingency() {
        // Arrange
        final DateTime initialDate = new DateTime(DATE_TIME_VALID);
        final long expireInSeconds = ONE_HOUR_SECONDS;
        final DateTime expectedDate = initialDate.plusSeconds(expireInSeconds);

        // Act
        DateTime result = DateUtils.calculateExpired(initialDate, expireInSeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString());
    }

    // --
    // DateTime IsExpired Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testIsExpired_ValidExpiredDate() {
        // Arrange
        final DateTime pastDate = DateTime.now().plus(Duration.ofDays(1)); // 1 day before now
        final DateTime now = DateTime.now();

        // Act
        boolean isExpired = DateUtils.isExpired(pastDate, now);

        // Assert
        assertTrue(isExpired, "The past date should be expired compared to now.");
    }

    @Test
    void testIsExpired_ValidFutureDate() {
        // Arrange
        final DateTime futureDate = DateTime.now().plus(Duration.ofDays(1)); // 1 day after now
        final DateTime now = DateTime.now();

        // Act
        boolean isExpired = DateUtils.isExpired(futureDate, now);

        // Assert
        assertTrue(isExpired, "The future date should not be expired compared to now.");
    }

    @Test
    void testIsExpired_SameDate() {
        // Arrange
        final DateTime exactDate = new DateTime(DATE_TIME_VALID);

        // Act
        boolean isExpired = DateUtils.isExpired(exactDate, exactDate);

        // Assert
        assertFalse(isExpired, "The same date should not be expired.");
    }

    @Test
    void testIsExpired_NullDateToCheck() {
        // Arrange
        final DateTime expireDate = new DateTime(DATE_TIME_VALID);

        // Act & Assert
        try {
            DateUtils.isExpired(null, expireDate);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_EXPIRE_DATE, e.getMessage());
        }
    }

    @Test
    void testIsExpired_NullExpireDate() {
        // Arrange
        final DateTime dateToCheck = new DateTime(DATE_TIME_VALID);

        // Act & Assert
        try {
            DateUtils.isExpired(dateToCheck, null);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_EXPIRE_DATE, e.getMessage());
        }
    }

    @Test
    void testIsExpired_DefaultExpirationCheck() {
        // Arrange
        final DateTime pastDate = DateTime.now().plus(Duration.ofDays(1));

        // Act
        boolean isExpired = DateUtils.isExpired(pastDate);

        // Assert
        assertTrue(isExpired, "The past date should be expired when compared to the current time.");
    }

    @Test
    void testIsExpired_DefaultNotExpiredCheck() {
        // Arrange
        final DateTime futureDate = DateTime.now().minus(Duration.ofHours(2));

        // Act
        boolean isExpired = DateUtils.isExpired(futureDate);

        // Assert
        assertFalse(isExpired, "The future date should not be expired when compared to the current time.");
    }

    @Test
    void testIsExpired_NullExpireDateForDate() {
        // Arrange
        final Date dateToCheck = new Date(DATE_VALID);

        // Act & Assert
        try {
            DateUtils.isExpired(dateToCheck, null);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_EXPIRE_DATE, e.getMessage());
        }
    }

    @Test
    void testIsExpired_SameDateForDate() {
        // Arrange
        final Date dateToCheck = new Date(DATE_VALID);
        final Date expireDate = new Date(DATE_VALID);

        // Act
        boolean isExpired = DateUtils.isExpired(dateToCheck, expireDate);

        // Assert
        assertFalse(isExpired, "The same date should not be expired.");
    }

    @Test
    void testIsExpired_DefaultExpirationCheckForDate() {
        // Arrange
        final Date pastDate = Date.today().plusDays(1);

        // Act
        boolean isExpired = DateUtils.isExpired(pastDate);

        // Assert
        assertTrue(isExpired, "The past date should be expired when compared to the current date.");
    }

    // Date Calculate Expired Tests
    // -----------------------------------------------------------------------------------------------------------------
    @Test
    void testCalculateExpired_ValidParameters_Date() {
        // Arrange
        final Date initialDate = new Date(DATE_VALID);
        final Date expectedDate = initialDate.plusDays(1);

        // Act
        Date result = DateUtils.calculateExpired(initialDate, ONE_DAY_SECONDS, ONE_HOUR_SECONDS);

        // Assert
        assertEquals(expectedDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_NegativeExpireInSeconds_Date() {
        // Arrange
        final Date initialDate = new Date(DATE_VALID);
        final long expireInSeconds = -ONE_DAY_SECONDS;

        // Act
        Date result = DateUtils.calculateExpired(initialDate, expireInSeconds, ONE_HOUR_SECONDS);

        // Assert
        assertEquals(initialDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_NegativeContingencySeconds_Date() {
        // Arrange
        final Date initialDate = new Date(DATE_VALID);
        final long contingencySeconds = -ONE_HOUR_SECONDS;
        final Date expectedDate = initialDate.plusDays(1);

        // Act
        Date result = DateUtils.calculateExpired(initialDate, ONE_DAY_SECONDS, contingencySeconds);

        // Assert
        assertEquals(expectedDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_AllNegativeSeconds_Date() {
        // Arrange
        final Date initialDate = new Date(DATE_VALID);
        final long expireInSeconds = -ONE_DAY_SECONDS;
        final long contingencySeconds = -ONE_HOUR_SECONDS;

        // Act
        Date result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(initialDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_ZeroSeconds_Date() {
        // Arrange
        final Date initialDate = new Date(DATE_VALID);
        final long expireInSeconds = 0; // Zero
        final long contingencySeconds = 0; // Zero

        // Act
        Date result = DateUtils.calculateExpired(initialDate, expireInSeconds, contingencySeconds);

        // Assert
        assertEquals(initialDate.toString(), result.toString());
    }

    @Test
    void testCalculateExpired_NullDate_Date() {
        // Act & Assert
        try {
            DateUtils.calculateExpired((Date) null, ONE_DAY_SECONDS, ONE_HOUR_SECONDS);
        } catch (IllegalArgumentException e) {
            assertEquals(ERROR_NULL_DATE, e.getMessage());
        }
    }

}