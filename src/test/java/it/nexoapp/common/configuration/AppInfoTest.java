package it.nexoapp.common.configuration;

import io.smallrye.config.ConfigValidationException;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import it.nexoapp.common.constant.Environment;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppInfoTest {

    @Test
    void mapAllProperties() {
        final AppInfo appInfo = buildAppInfo(validProperties());

        assertEquals("nexo", appInfo.code(), "app.code should be mapped");
        assertEquals("Nexo Sport", appInfo.name(), "app.name should be mapped");
        assertEquals("Nexo Sport backend", appInfo.description(), "app.description should be mapped");
        assertEquals(Environment.PROD, appInfo.environment(), "app.environment should be mapped through the converter");
        assertEquals("my-host", appInfo.hostname(), "app.hostname should be mapped through the converter");
        assertEquals(Level.DEBUG, appInfo.logLevel(), "app.log.level should be mapped");
    }

    @Test
    void environmentConversionIsCaseInsensitive() {
        final Map<String, String> properties = validProperties();
        properties.put("app.environment", "Sit");

        assertEquals(Environment.SIT, buildAppInfo(properties).environment(), "app.environment should be case-insensitive");
    }

    @Test
    void logLevelDefaultsToInfo() {
        final Map<String, String> properties = validProperties();
        properties.remove("app.log.level");

        assertEquals(Level.INFO, buildAppInfo(properties).logLevel(), "app.log.level should default to INFO");
    }

    @Test
    void missingRequiredPropertyThrows() {
        final Map<String, String> properties = validProperties();
        properties.remove("app.code");

        assertThrows(ConfigValidationException.class, () -> buildAppInfo(properties), "app.code has no default and should be required");
    }

    @Test
    void invalidEnvironmentThrows() {
        final Map<String, String> properties = validProperties();
        properties.put("app.environment", "staging");

        assertThrows(ConfigValidationException.class, () -> buildAppInfo(properties), "An unknown environment should fail the mapping validation");
    }

    @Test
    void invalidLogLevelThrows() {
        final Map<String, String> properties = validProperties();
        properties.put("app.log.level", "VERBOSE");

        assertThrows(ConfigValidationException.class, () -> buildAppInfo(properties), "An unknown log level should fail the mapping validation");
    }

    private static AppInfo buildAppInfo(final Map<String, String> properties) {
        final SmallRyeConfig config = new SmallRyeConfigBuilder()
                .withMapping(AppInfo.class)
                .withSources(new PropertiesConfigSource(properties, "test-source", 100))
                .build();

        return config.getConfigMapping(AppInfo.class);
    }

    private static Map<String, String> validProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("app.code", "nexo");
        properties.put("app.name", "Nexo Sport");
        properties.put("app.description", "Nexo Sport backend");
        properties.put("app.environment", "prod");
        properties.put("app.hostname", "my-host");
        properties.put("app.log.level", "DEBUG");
        return properties;
    }

}