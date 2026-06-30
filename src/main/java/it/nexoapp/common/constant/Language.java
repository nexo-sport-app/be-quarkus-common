package it.nexoapp.common.constant;

import it.nexoapp.common.utils.NumberUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * Enumeration representing supported languages in the application.
 * <p>
 * This enum should be used throughout the application whenever language selection
 * or localization is required. Each language constant is associated with a specific
 * {@link Locale} that can be used for formatting numbers, dates, and other locale-sensitive
 * operations.
 * </p>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Get the default language
 * Language defaultLang = Language.getDefault();
 *
 * // Get language from a Locale
 * Language lang = Language.fromLocale(Locale.ENGLISH);
 *
 * // Access the underlying Locale
 * Locale locale = Language.IT.getLocale();
 *
 * // Use with NumberUtils for localized formatting
 * BigDecimal value = NumberUtils.of(1234.56);
 * String formatted = NumberUtils.formatLocalized(value, Language.IT.getLocale(), 2, 2);
 * }</pre>
 *
 * @see Locale
 * @see NumberUtils
 */
@Getter
@RequiredArgsConstructor
public enum Language {

    /**
     * English language constant.
     * Associated with {@link Locale#ENGLISH}.
     */
    EN(Locale.ENGLISH),

    /**
     * Italian language constant (default).
     * Associated with {@link Locale#ITALIAN}.
     */
    IT(Locale.ITALIAN)
    ;

    /**
     * The Java Locale associated with this language.
     * Used for locale-sensitive operations such as number and date formatting.
     */
    private final Locale locale;

    /**
     * Returns the default language for the application.
     *
     * @return The default language, which is Italian (IT)
     */
    public static Language getDefault() {
        return IT;
    }

    /**
     * Converts a {@link Locale} to its corresponding Language enum constant.
     * <p>
     * If the provided locale does not match any supported language,
     * the default language is returned.
     * </p>
     *
     * @param locale The locale to convert
     * @return The corresponding Language enum constant, or the default language if no match is found
     * @see #getDefault()
     */
    public static Language fromLocale(final Locale locale) {
        for (Language language : values()) {
            if (language.getLocale().equals(locale)) {
                return language;
            }
        }
        return getDefault();
    }

}