package it.nexoapp.common.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Represents a monetary amount with a specified value and currency.
 * <p>
 * This class provides constructors for creating an Amount instance with different types
 * of value inputs (e.g., BigDecimal, int, double, float, long, String) and currency definitions.
 * It also includes methods for performing arithmetic operations such as addition, subtraction,
 * multiplication, and division, with type consistency enforced by currencies.
 */
@Data
public class Amount implements Serializable {

    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

    @Serial
    private static final long serialVersionUID = 312809438342271101L;

    @NotNull
    private BigDecimal value;
    @NotNull
    private Currency currency;

    // --
    // CONSTRUCTOR
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new Amount with specified BigDecimal value and Currency
     *
     * @param value    The amount value as BigDecimal
     * @param currency The currency
     */
    public Amount(final BigDecimal value, final Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    /**
     * Creates a new Amount with specified BigDecimal value and currency code
     *
     * @param value    The amount value as BigDecimal
     * @param currency The currency code as String
     */
    public Amount(final BigDecimal value, final String currency) {
        this.value = value;
        this.currency = Currency.getInstance(currency);
    }

    /**
     * Creates a new Amount with specified integer value and Currency
     *
     * @param value    The amount value as integer
     * @param currency The currency
     */
    public Amount(final int value, final Currency currency) {
        this(new BigDecimal(value), currency);
    }

    /**
     * Creates a new Amount with specified integer value and currency code
     *
     * @param value    The amount value as integer
     * @param currency The currency code as String
     */
    public Amount(final int value, final String currency) {
        this(new BigDecimal(value), currency);
    }

    /**
     * Creates a new Amount with specified double value and Currency
     *
     * @param value    The amount value as double
     * @param currency The currency
     */
    public Amount(final double value, final Currency currency) {
        this(BigDecimal.valueOf(value), currency);
    }

    /**
     * Creates a new Amount with specified double value and currency code
     *
     * @param value    The amount value as double
     * @param currency The currency code as String
     */
    public Amount(final double value, final String currency) {
        this(BigDecimal.valueOf(value), currency);
    }

    /**
     * Creates a new Amount with specified float value and Currency
     *
     * @param value    The amount value as float
     * @param currency The currency
     */
    public Amount(final float value, final Currency currency) {
        this(BigDecimal.valueOf(value), currency);
    }

    /**
     * Creates a new Amount with specified float value and currency code
     *
     * @param value    The amount value as float
     * @param currency The currency code as String
     */
    public Amount(final float value, final String currency) {
        this(BigDecimal.valueOf(value), currency);
    }

    /**
     * Creates a new Amount with specified long value and Currency
     *
     * @param value    The amount value as long
     * @param currency The currency
     */
    public Amount(final long value, final Currency currency) {
        this(new BigDecimal(value), currency);
    }

    /**
     * Creates a new Amount with specified long value and currency code
     *
     * @param value    The amount value as long
     * @param currency The currency code as String
     */
    public Amount(final long value, final String currency) {
        this(new BigDecimal(value), currency);
    }

    /**
     * Creates a new Amount with specified String value and Currency
     *
     * @param value    The amount value as String
     * @param currency The currency
     */
    public Amount(final String value, final Currency currency) {
        this(new BigDecimal(value), currency);
    }

    /**
     * Creates a new Amount with specified String value and currency code
     *
     * @param value    The amount value as String
     * @param currency The currency code as String
     */
    public Amount(final String value, final String currency) {
        this(new BigDecimal(value), currency);
    }

    // --
    // STATIC METHODS
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new Amount instance with the specified value and default EUR currency
     *
     * @param value The BigDecimal value
     * @return new Amount instance
     */
    public static Amount of(final BigDecimal value) {
        return new Amount(value, DEFAULT_CURRENCY);
    }

    // --
    // INSTANCE METHODS
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of this Amount using currency code
     *
     * @return String in format "value currencyCode"
     */
    @Override
    public String toString() {
        return value.toPlainString() + " " + currency.getCurrencyCode();
    }

    /**
     * Returns a string representation of this Amount using currency symbol
     *
     * @return String in format "value currencySymbol"
     */
    public String toUI() {
        return value.toPlainString() + " " + currency.getSymbol();
    }

    /**
     * Returns a new Amount normalized to the currency's default fraction digits using HALF_UP.
     */
    public Amount normalized() {
        final int scale = currency.getDefaultFractionDigits();
        final BigDecimal v = (scale >= 0 ? value.setScale(scale, RoundingMode.HALF_UP) : value);
        return new Amount(v, currency);
    }

    // --
    // MUTATING ARITHMETIC (IN PLACE)
    // -----------------------------------------------------------------------------------------------------------------

    private void ensureSameCurrency(final Amount other) {
        if (!this.currency.equals(other.getCurrency())) {
            throw new IllegalArgumentException("Cannot operate on amounts with different currencies");
        }
    }

    /**
     * Adds the specified BigDecimal amount to this Amount's value
     *
     * @param amount The BigDecimal value to add
     */
    public void add(final BigDecimal amount) {
        this.value = this.value.add(amount);
    }

    /**
     * Adds another Amount to this Amount if they have the same currency
     *
     * @param amount The Amount to add
     * @throws IllegalArgumentException if currencies are different
     */
    public void add(final Amount amount) {
        ensureSameCurrency(amount);
        this.value = this.value.add(amount.getValue());
    }

    /**
     * Subtracts the specified BigDecimal amount from this Amount's value
     *
     * @param amount The BigDecimal value to subtract
     */
    public void subtract(final BigDecimal amount) {
        this.value = this.value.subtract(amount);
    }

    /**
     * Subtracts another Amount from this Amount if they have the same currency
     *
     * @param amount The Amount to subtract
     * @throws IllegalArgumentException if currencies are different
     */
    public void subtract(final Amount amount) {
        ensureSameCurrency(amount);
        this.value = this.value.subtract(amount.getValue());
    }

    /**
     * Multiplies this Amount's value by the specified BigDecimal amount
     *
     * @param amount The BigDecimal value to multiply by
     */
    public void multiply(final BigDecimal amount) {
        this.value = this.value.multiply(amount);
    }

    /**
     * Multiplies this Amount by another Amount if they have the same currency
     *
     * @param amount The Amount to multiply by
     * @throws IllegalArgumentException if currencies are different
     */
    public void multiply(final Amount amount) {
        ensureSameCurrency(amount);
        this.value = this.value.multiply(amount.getValue());
    }

    /**
     * Divides this Amount's value by the specified BigDecimal amount
     *
     * @param amount The BigDecimal value to divide by
     */
    public void divide(final BigDecimal amount) {
        this.value = this.value.divide(amount, currency.getDefaultFractionDigits(), RoundingMode.HALF_DOWN);
    }

    /**
     * Divides this Amount by another Amount if they have the same currency
     *
     * @param amount The Amount to divide by
     * @throws IllegalArgumentException if currencies are different
     */
    public void divide(final Amount amount){
        ensureSameCurrency(amount);
        this.value = this.value.divide(amount.getValue(), currency.getDefaultFractionDigits(), RoundingMode.HALF_DOWN);
    }

}