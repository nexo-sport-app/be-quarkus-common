package it.nexoapp.common.configuration;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import it.nexoapp.common.configuration.coverter.EnvironmentConverter;
import it.nexoapp.common.configuration.coverter.HostnameConverter;
import it.nexoapp.common.constant.Environment;
import org.slf4j.event.Level;

/**
 * Application metadata, mapped from configuration properties with the {@code app} prefix.
 * <p>
 * {@link #code()}, {@link #name()} and {@link #description()} must be configured explicitly
 * by each microservice (e.g. in {@code application.properties}). {@link #environment()} and
 * {@link #hostname()} are resolved automatically but can be overridden via configuration,
 * while {@link #logLevel()} falls back to a default when not set.
 */
@StaticInitSafe
@ConfigMapping(prefix = "app")
public interface AppInfo {

    /**
     * Short unique identifier of the microservice (property {@code app.code}).
     * Must be configured by each microservice.
     */
    String code();

    /**
     * Human-readable name of the microservice (property {@code app.name}).
     * Must be configured by each microservice.
     */
    String name();

    /**
     * Brief description of the microservice's purpose (property {@code app.description}).
     * Must be configured by each microservice.
     */
    String description();

    /**
     * Environment the application is running in (property {@code app.environment}).
     * Resolved automatically from the active {@code quarkus.profile} by
     * {@link EnvironmentConverter}; set the property explicitly to override.
     */
    @WithConverter(EnvironmentConverter.class)
    Environment environment();

    /**
     * Hostname of the machine running the application (property {@code app.hostname}).
     * Resolved automatically by {@link HostnameConverter} from the {@code HOSTNAME}
     * system property or the local machine name; set the property explicitly to override.
     */
    @WithConverter(HostnameConverter.class)
    String hostname();

    /**
     * Application log level (property {@code app.log.level}).
     * Optional, defaults to {@code INFO}.
     */
    @WithDefault("INFO")
    @WithName("log.level")
    Level logLevel();

}