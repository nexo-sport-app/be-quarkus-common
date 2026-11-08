package it.nexoapp.common.configuration;

import io.smallrye.config.ConfigValidationException;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the platform defaults that common ships in {@code META-INF/microprofile-config.properties}:
 * they are the lowest-priority source, a service value always wins, and the service must configure
 * {@code app.code} itself.
 */
class ConfigDefaultsTest {

    /** Classpath location of the platform defaults. */
    private static final String DEFAULTS = "/META-INF/microprofile-config.properties";

    /** Keys a service has to provide, used to expand the placeholders of the defaults. */
    private static final Map<String, String> SERVICE_APP = Map.of(
            "app.code", "svc",
            "app.name", "Service",
            "app.description", "Service description");

    // ------------------------------------------------------------------------------------------

    /**
     * A value set by a service (higher ordinal) overrides the corresponding platform default.
     */
    @Test
    void serviceValueWinsOverPlatformDefault() throws IOException {
        final SmallRyeConfig defaults = build(null, Map.of());
        assertEquals("validate", defaults.getRawValue("quarkus.hibernate-orm.schema-management.strategy"),
                "The platform default should validate the schema");

        final SmallRyeConfig overridden = build(null,
                Map.of("quarkus.hibernate-orm.schema-management.strategy", "none"));
        assertEquals("none", overridden.getRawValue("quarkus.hibernate-orm.schema-management.strategy"),
                "The service value should win over the platform default");
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Swagger and OpenAPI are permitted without authentication only with the dev profile.
     */
    @Test
    void publicPermitRuleExistsOnlyInDevProfile() throws IOException {
        final SmallRyeConfig dev = build("dev", Map.of());
        assertEquals("permit", dev.getRawValue("quarkus.http.auth.permission.public.policy"),
                "The public permit policy should exist in dev");
        assertTrue(dev.getRawValue("quarkus.http.auth.permission.public.paths").contains("/nexo-svc/swagger"),
                "The public paths should cover Swagger UI");

        final SmallRyeConfig prod = build("prod", Map.of());
        assertFalse(prod.getOptionalValue("quarkus.http.auth.permission.public.policy", String.class).isPresent(),
                "The public permit policy should be absent in prod");
        assertFalse(prod.getOptionalValue("quarkus.http.auth.permission.public.paths", String.class).isPresent(),
                "The public permit paths should be absent in prod");
        assertEquals("authenticated", prod.getRawValue("quarkus.http.auth.permission.authenticated.policy"),
                "Every other path should require authentication");
    }

    // ------------------------------------------------------------------------------------------

    /**
     * The defaults set all-violations validation, INFO logging and no Swagger UI in builds.
     */
    @Test
    void platformDefaultsAreSet() throws IOException {
        final SmallRyeConfig config = build(null, Map.of());

        assertEquals("false", config.getRawValue("quarkus.hibernate-validator.fail-fast"),
                "All violations should be reported");
        assertEquals("false", config.getRawValue("quarkus.swagger-ui.always-include"),
                "Swagger UI should not be included in builds");
        assertEquals("INFO", config.getValue("quarkus.log.category.\"it\".level", String.class),
                "The default log level should be INFO");
        assertEquals("/q", config.getRawValue("quarkus.http.non-application-root-path"),
                "Health should be served under the absolute /q root");
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Common provides no default for {@code app.code}: without it the {@link AppInfo} mapping fails.
     */
    @Test
    void missingAppCodeFailsValidation() throws IOException {
        final SmallRyeConfigBuilder builder = new SmallRyeConfigBuilder()
                .withMapping(AppInfo.class)
                .withSources(defaultsSource())
                .withSources(new PropertiesConfigSource(
                        Map.of("app.name", "Service", "app.description", "Service description",
                                "app.environment", "prod", "app.hostname", "host"), "service", 250));

        assertThrows(ConfigValidationException.class, builder::build,
                "app.code should be required from the service");
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Builds a config with the platform defaults (ordinal 100) and a service source (ordinal 250).
     *
     * @param profile  active profile, or {@code null} for none
     * @param override service properties added on top of {@link #SERVICE_APP}
     * @return the built config
     */
    private static SmallRyeConfig build(final String profile, final Map<String, String> override) throws IOException {
        final java.util.HashMap<String, String> service = new java.util.HashMap<>(SERVICE_APP);
        service.putAll(override);

        final SmallRyeConfigBuilder builder = new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .withSources(defaultsSource())
                .withSources(new PropertiesConfigSource(service, "service", 250));
        if (profile != null) {
            builder.withProfile(profile);
        }
        return builder.build();
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Loads the platform defaults from the classpath at ordinal 100.
     *
     * @return the config source
     */
    private static PropertiesConfigSource defaultsSource() throws IOException {
        final URL url = ConfigDefaultsTest.class.getResource(DEFAULTS);
        assertNotNull(url, "common should ship " + DEFAULTS);
        return new PropertiesConfigSource(url, 100);
    }

}
