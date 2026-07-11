package it.nexoapp.common.configuration.coverter;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import it.nexoapp.common.constant.Environment;
import org.apache.commons.lang3.Strings;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentConverterTest {

    private static final String QUARKUS_PROFILE = "quarkus.profile";

    private final EnvironmentConverter converter = new EnvironmentConverter();

    private SmallRyeConfig registeredConfig;

    /*
     * The test run itself activates the 'test' quarkus profile, which the converter would pick up
     * through ConfigProvider.getConfig(): register an isolated config so every test starts
     * without an active profile.
     */
    @BeforeEach
    void setUp() {
        registerConfig(Map.of());
    }

    @AfterEach
    void tearDown() {
        releaseRegisteredConfig();
    }

    /* manual value */

    @ParameterizedTest
    @EnumSource(Environment.class)
    void convertManualValue(final Environment environment) {
        assertEquals(environment, converter.convert(environment.name()), "Every environment should be converted from its own name");
    }

    @ParameterizedTest
    @ValueSource(strings = {"prod", "Prod", "pRoD"})
    void convertManualValueIsCaseInsensitive(final String value) {
        assertEquals(Environment.PROD, converter.convert(value), "Conversion should be case-insensitive");
    }

    @Test
    void convertInvalidManualValueThrows() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert("STAGING"), "An unknown environment should not be accepted");
    }

    @Test
    void convertManualValueWinsOverQuarkusProfile() {
        setQuarkusProfile("prod");
        assertEquals(Environment.SIT, converter.convert("sit"), "The manual value should have priority over the quarkus profile");
    }

    /* quarkus profile fallback */

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void convertBlankValueUsesQuarkusProfile(final String value) {
        setQuarkusProfile("uat");
        assertEquals(Environment.UAT, converter.convert(value), "A blank value should fall back to the quarkus profile");
    }

    @Test
    void convertBlankValueUsesFirstQuarkusProfileToken() {
        setQuarkusProfile("prod,common");
        assertEquals(Environment.PROD, converter.convert(null), "Only the first token of a multi-profile value should be used");
    }

    @Test
    void convertBlankValueWithQuarkusProfileIsCaseInsensitive() {
        setQuarkusProfile("dev");
        assertEquals(Environment.DEV, converter.convert(null), "The quarkus profile should be matched case-insensitively");
    }

    @Test
    void convertBlankValueWithInvalidQuarkusProfileThrows() {
        setQuarkusProfile("staging");
        assertThrows(IllegalArgumentException.class, () -> converter.convert(null), "An unknown quarkus profile should not be accepted");
    }

    @Test
    void convertBlankValueWithoutQuarkusProfileThrows() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(null),
                "Without a manual value nor a quarkus profile the conversion should fail"
        );

        assertTrue(Strings.CS.contains(exception.getMessage(), "app.environment"), "The error message should mention the manual property");
        assertTrue(Strings.CS.contains(exception.getMessage(), "quarkus.profile"), "The error message should mention the quarkus profile property");
    }

    private void setQuarkusProfile(final String profile) {
        registerConfig(Map.of(QUARKUS_PROFILE, profile));
    }

    /* bind a config with exactly the given properties to the test classloader, replacing any cached one */
    private void registerConfig(final Map<String, String> properties) {
        final ConfigProviderResolver resolver = ConfigProviderResolver.instance();

        releaseRegisteredConfig();
        resolver.releaseConfig(resolver.getConfig());

        final SmallRyeConfig config = new SmallRyeConfigBuilder()
                .withSources(new PropertiesConfigSource(properties, "test-source", 100))
                .build();

        resolver.registerConfig(config, Thread.currentThread().getContextClassLoader());
        registeredConfig = config;
    }

    private void releaseRegisteredConfig() {
        if (registeredConfig != null) {
            ConfigProviderResolver.instance().releaseConfig(registeredConfig);
            registeredConfig = null;
        }
    }

}