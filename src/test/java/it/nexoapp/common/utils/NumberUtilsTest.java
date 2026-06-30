package it.nexoapp.common.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilsTest {

    // --
    // Conversion Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testOfLong() {
        assertEquals(new BigDecimal("123456789"), NumberUtils.of(123456789L));
    }

    @Test
    void testOfDouble() {
        assertEquals(0, new BigDecimal("12345.67").compareTo(NumberUtils.of(12345.67)));
    }

    @Test
    void testOfString() {
        assertEquals(new BigDecimal("987654.321"), NumberUtils.of("987654.321"));
    }

    @Test
    void testOfStringNull() {
        assertThrows(NullPointerException.class, () -> NumberUtils.of((String) null));
    }

    @Test
    void testOfNumberLong() {
        assertEquals(new BigDecimal("999999999"), NumberUtils.of((Number) 999999999L));
    }

    @Test
    void testOfNumberFloat() {
        assertEquals(0, new BigDecimal("123.45").compareTo(NumberUtils.of((Number) 123.45f).setScale(2, RoundingMode.HALF_EVEN)));
    }

    @Test
    void testOfNumberBigDecimal() {
        BigDecimal input = new BigDecimal("456.789");
        assertSame(input, NumberUtils.of((Number) input));
    }

    @Test
    void testOfNumberNull() {
        assertThrows(NullPointerException.class, () -> NumberUtils.of((Number) null));
    }

    @Test
    void testOfNumberInteger() {
        assertEquals(new BigDecimal("123456"), NumberUtils.of((Number) 123456));
    }

    @Test
    void testOfCustomNumber() {
        Number input = new Number() {
            @Override
            public int intValue() {
                return 0;
            }

            @Override
            public long longValue() {
                return 789;
            }

            @Override
            public float floatValue() {
                return 0;
            }

            @Override
            public double doubleValue() {
                return 0;
            }

            @Override
            public String toString() {
                return "789.01";
            }
        };
        assertEquals(new BigDecimal("789.01"), NumberUtils.of(input));
    }

    // --
    // Scale Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testSetScaleWithBigDecimal() {
        assertEquals(new BigDecimal("123.46"), NumberUtils.setScale(new BigDecimal("123.456789"), 2));
    }

    @Test
    void testSetScaleWithCurrency() {
        assertEquals(new BigDecimal("123.46"), NumberUtils.setScale(new BigDecimal("123.456789"), Currency.getInstance("USD")));
    }

    // --
    // Zero/Positive/Negative Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testIsZeroWithZeroValue() {
        assertTrue(NumberUtils.isZero(BigDecimal.ZERO));
    }

    @Test
    void testIsZeroWithNonZeroValue() {
        assertFalse(NumberUtils.isZero(BigDecimal.ONE));
        assertFalse(NumberUtils.isZero(new BigDecimal("-1")));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testIsZeroWithNullValue() {
        assertFalse(NumberUtils.isZero(null));
    }

    @Test
    void testIsPositiveWithPositiveValue() {
        assertTrue(NumberUtils.isPositive(new BigDecimal("1.23")));
    }

    @Test
    void testIsPositiveWithNegativeValue() {
        assertFalse(NumberUtils.isPositive(new BigDecimal("-1.23")));
    }

    @Test
    void testIsPositiveWithZeroValue() {
        assertFalse(NumberUtils.isPositive(BigDecimal.ZERO));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testIsPositiveWithNullValue() {
        assertFalse(NumberUtils.isPositive(null));
    }

    @Test
    void testIsNegativeWithNegativeValue() {
        assertTrue(NumberUtils.isNegative(new BigDecimal("-1.23")));
    }

    @Test
    void testIsNegativeWithPositiveValue() {
        assertFalse(NumberUtils.isNegative(new BigDecimal("1.23")));
    }

    @Test
    void testIsNegativeWithZeroValue() {
        assertFalse(NumberUtils.isNegative(BigDecimal.ZERO));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testIsNegativeWithNullValue() {
        assertFalse(NumberUtils.isNegative(null));
    }

    // --
    // Comparison Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testCompareWithNonNullValues() {
        assertEquals(-1, NumberUtils.compare(new BigDecimal("5.00"), new BigDecimal("10.00")));
        assertEquals(1, NumberUtils.compare(new BigDecimal("10.00"), new BigDecimal("5.00")));
        assertEquals(0, NumberUtils.compare(new BigDecimal("5.00"), new BigDecimal("5.00")));
    }

    @Test
    void testCompareWithFirstValueNull() {
        assertEquals(-1, NumberUtils.compare(null, new BigDecimal("10.00")));
    }

    @Test
    void testCompareWithSecondValueNull() {
        assertEquals(1, NumberUtils.compare(new BigDecimal("5.00"), null));
    }

    @Test
    void testCompareWithBothValuesNull() {
        assertEquals(0, NumberUtils.compare(null, null));
    }

    // --
    // Null Handling Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testNullToZeroWithNull() {
        assertEquals(BigDecimal.ZERO, NumberUtils.nullToZero(null));
    }

    @Test
    void testNullToZeroWithNonNullValue() {
        BigDecimal input = new BigDecimal("123.45");
        assertSame(input, NumberUtils.nullToZero(input));
    }

    @Test
    void testNullToZeroWithEdgeNumbers() {
        final BigDecimal n1 = new BigDecimal("1E+30");
        assertEquals(n1, NumberUtils.nullToZero(n1));

        BigDecimal n2 = new BigDecimal("1E-30");
        assertEquals(n2, NumberUtils.nullToZero(n2));
    }

    // --
    // Division Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testSafeDivideWithValidInputs() {
        assertEquals(new BigDecimal("3.33"), NumberUtils.safeDivide(new BigDecimal("10.00"), new BigDecimal("3.00"), 2));
    }

    @Test
    void testSafeDivideWithZeroDividend() {
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN), NumberUtils.safeDivide(BigDecimal.ZERO, new BigDecimal("5.00"), 2));
    }

    @Test
    void testSafeDivideWithZeroDivisor() {
        final BigDecimal val1 = new BigDecimal("10.00");
        assertThrows(ArithmeticException.class, () -> NumberUtils.safeDivide(val1, BigDecimal.ZERO, 2));
    }

    @Test
    void testSafeDivideWithNegativeInputs() {
        assertEquals(new BigDecimal("-2.50"), NumberUtils.safeDivide(new BigDecimal("-10.00"), new BigDecimal("4.00"), 2));
    }

    @Test
    void testSafeDivideWithRoundUp() {
        assertEquals(new BigDecimal("3.3"), NumberUtils.safeDivide(new BigDecimal("10.00"), new BigDecimal("3.00"), 1));
    }

    // --
    // Percentage Calculation Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testPercentageValidInputs() {
        assertEquals(new BigDecimal("31"), NumberUtils.percentage(new BigDecimal("200"), new BigDecimal("15.5")));
    }

    @Test
    void testPercentageBaseZero() {
        assertEquals(BigDecimal.ZERO, NumberUtils.percentage(BigDecimal.ZERO, new BigDecimal("20")));
    }

    @Test
    void testPercentagePercentZero() {
        assertEquals(BigDecimal.ZERO, NumberUtils.percentage(new BigDecimal("100"), BigDecimal.ZERO));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testPercentageWithNullValues() {
        final BigDecimal val1 = new BigDecimal("20");
        assertThrows(NullPointerException.class, () -> NumberUtils.percentage(null,val1));
        final BigDecimal val2 = new BigDecimal("100");
        assertThrows(NullPointerException.class, () -> NumberUtils.percentage(val2, null));
    }

    @Test
    void testPercentageWithNegativeInputs() {
        assertEquals(new BigDecimal("-30"), NumberUtils.percentage(new BigDecimal("150"), new BigDecimal("-20")));
        assertEquals(new BigDecimal("-30"), NumberUtils.percentage(new BigDecimal("-150"), new BigDecimal("20")));
    }

    @Test
    void testPercentageOfWithValidInputs() {
        assertEquals(new BigDecimal("30.00"), NumberUtils.percentageOf(new BigDecimal("0.15"), new BigDecimal("200")));
    }

    @Test
    void testPercentageOfWithZeroPercent() {
        assertEquals(BigDecimal.ZERO, NumberUtils.percentageOf(BigDecimal.ZERO, new BigDecimal("200")));
    }

    @Test
    void testPercentageOfWithZeroBase() {
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN), NumberUtils.percentageOf(new BigDecimal("0.15"), BigDecimal.ZERO));
    }

    @Test
    void testPercentageOfWithNegativeValues() {
        assertEquals(new BigDecimal("-30.00"), NumberUtils.percentageOf(new BigDecimal("-0.15"), new BigDecimal("200")));
        assertEquals(new BigDecimal("-30.00"), NumberUtils.percentageOf(new BigDecimal("0.15"), new BigDecimal("-200")));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testPercentageOfWithNullValues() {
        final BigDecimal baseValue = new BigDecimal("200");
        assertThrows(NullPointerException.class, () -> NumberUtils.percentageOf(null, baseValue));
        final BigDecimal percentage = new BigDecimal("0.15");
        assertThrows(NullPointerException.class, () -> NumberUtils.percentageOf(percentage, null));
    }

    // --
    // Localization Tests
    // -----------------------------------------------------------------------------------------------------------------

    @Test
    void testParseLocalizedValidInput() throws ParseException {
        assertEquals(new BigDecimal("1234.56"), NumberUtils.parseLocalized("1.234,56", Locale.GERMAN));
    }

    @Test
    void testParseLocalizedInvalidInput() {
        assertThrows(ParseException.class, () -> NumberUtils.parseLocalized("InvalidNumber", Locale.US));
    }

    @Test
    void testParseLocalizedWithNullInput() {
        assertThrows(NullPointerException.class, () -> NumberUtils.parseLocalized(null, Locale.US));
    }

    @Test
    void testFormatLocalizedWithValidInputs() {
        assertEquals("123,456.789", NumberUtils.formatLocalized(new BigDecimal("123456.789"), Locale.US, 1, 3));
    }

    @Test
    void testFormatLocalizedWithEdgeCases() {
        assertEquals("12,345,678,901,234,567,890.1235", NumberUtils.formatLocalized(new BigDecimal("12345678901234567890.123456"), Locale.US, 2, 4));
        assertEquals("0.0001", NumberUtils.formatLocalized(new BigDecimal("0.000123456"), Locale.US, 2, 4));
    }

    @Test
    void testFormatLocalizedWithNullValue() {
        assertThrows(Exception.class, () -> NumberUtils.formatLocalized(null, Locale.US, 1, 3));
    }

    @Test
    void testFormatLocalizedWithInvalidLocale() {
        final BigDecimal amount = new BigDecimal("12345.67");
        assertThrows(NullPointerException.class, () -> NumberUtils.formatLocalized(amount, null, 1, 3));
    }

}