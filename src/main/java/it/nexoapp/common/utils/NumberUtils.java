package it.nexoapp.common.utils;

import it.nexoapp.common.exception.IllegalInstantiationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for working with numbers, specifically for performing safe and
 * precise operations with BigDecimal objects and localized parsing/formatting.
 * Provides various methods for creating, scaling, comparing, and manipulating
 * BigDecimal values, as well as for parsing and formatting localized number strings.
 */
public class NumberUtils {

    // -----------------------------------------------------------------------------------------------------------------

    private NumberUtils(){
        throw new IllegalInstantiationException();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a BigDecimal from a long value.
     *
     * @param value The long value to convert
     * @return A BigDecimal representing the long value
     */
    public static BigDecimal of(final long value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Creates a BigDecimal from a double value.
     *
     * @param value The double value to convert
     * @return A BigDecimal representing the double value
     */
    public static BigDecimal of(final double value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Creates a BigDecimal from a String value.
     *
     * @param value The String value to convert
     * @return A BigDecimal representing the String value
     * @throws NullPointerException if value is null
     */
    public static BigDecimal of(final String value) {
        Objects.requireNonNull(value, "value");
        return new BigDecimal(value.trim());
    }

    /**
     * Creates a BigDecimal from a Number value.
     *
     * @param value The Number value to convert
     * @return A BigDecimal representing the Number value
     * @throws NullPointerException if value is null
     */
    public static BigDecimal of(final Number value) {
        Objects.requireNonNull(value, "value");
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return BigDecimal.valueOf(value.longValue());
        }
        if (value instanceof Float || value instanceof Double) {
            return BigDecimal.valueOf(value.doubleValue());
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * Sets the scale of a BigDecimal value using HALF_UP rounding mode.
     *
     * @param value The BigDecimal to scale
     * @param scale The number of decimal places
     * @return A new BigDecimal with the specified scale
     */
    public static BigDecimal setScale(final BigDecimal value, final int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Sets the scale of a BigDecimal value based on the currency's default fraction digits.
     *
     * @param value    The BigDecimal to scale
     * @param currency The currency to determine the scale
     * @return A new BigDecimal with the currency-appropriate scale
     */
    public static BigDecimal setScale(final BigDecimal value, final Currency currency) {
        int scale = Math.max(currency.getDefaultFractionDigits(), 0);
        return setScale(value, scale);
    }

    /**
     * Checks if a BigDecimal value is zero.
     *
     * @param v The BigDecimal to check
     * @return true if the value is not null and equals zero, false otherwise
     */
    public static boolean isZero(final BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if a BigDecimal value is positive.
     *
     * @param v The BigDecimal to check
     * @return true if the value is not null and greater than zero, false otherwise
     */
    public static boolean isPositive(final BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if a BigDecimal value is negative.
     *
     * @param v The BigDecimal to check
     * @return true if the value is not null and less than zero, false otherwise
     */
    public static boolean isNegative(final BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Compares two BigDecimal values, handling null values.
     *
     * @param a First BigDecimal to compare
     * @param b Second BigDecimal to compare
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(final BigDecimal a, final BigDecimal b) {
        if (Objects.equals(a, b)) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    /**
     * Converts a null BigDecimal to zero, otherwise returns the original value.
     *
     * @param v The BigDecimal to check
     * @return Zero if the input is null, otherwise the input value
     */
    public static BigDecimal nullToZero(final BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * Performs division of BigDecimals with specified scale and HALF_UP rounding.
     *
     * @param dividend The number to be divided
     * @param divisor  The number to divide by
     * @param scale    The number of decimal places in the result
     * @return The result of the division with specified scale
     */
    public static BigDecimal safeDivide(final BigDecimal dividend, final BigDecimal divisor, final int scale) {
        return dividend.divide(divisor, Math.max(scale, 0), RoundingMode.HALF_UP);
    }

    /**
     * Calculates the percentage of a base value.
     *
     * @param base    The base value
     * @param percent The percentage to calculate
     * @return The calculated percentage value
     */
    public static BigDecimal percentage(final BigDecimal base, final BigDecimal percent) {
        return safeDivide(base.multiply(percent), BigDecimal.valueOf(100), base.scale());
    }

    /**
     * Calculates the result of applying a percentage to a base value.
     *
     * @param percent The percentage to apply
     * @param base    The base value
     * @return The result of multiplying base by percent
     */
    public static BigDecimal percentageOf(final BigDecimal percent, final BigDecimal base) {
        return base.multiply(percent);
    }

    // Parsing/formatting localizzato

    /**
     * Parses a localized number string into a BigDecimal.
     *
     * @param input  The string to parse
     * @param locale The locale to use for parsing
     * @return The parsed BigDecimal value
     * @throws ParseException if the string cannot be parsed
     */
    public static BigDecimal parseLocalized(final String input, final Locale locale) throws ParseException {
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        if (fmt instanceof DecimalFormat df) {
            df.setParseBigDecimal(true);
        }
        Number n = fmt.parse(input);
        return of(n);
    }

    /**
     * Formats a BigDecimal value according to the specified locale and fraction digits.
     *
     * @param value       The BigDecimal to format
     * @param locale      The locale to use for formatting
     * @param minFraction The minimum number of fraction digits
     * @param maxFraction The maximum number of fraction digits
     * @return The formatted string
     */
    public static String formatLocalized(final BigDecimal value, final Locale locale, final int minFraction, final int maxFraction) {
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        fmt.setMinimumFractionDigits(minFraction);
        fmt.setMaximumFractionDigits(maxFraction);
        return fmt.format(value);
    }

}