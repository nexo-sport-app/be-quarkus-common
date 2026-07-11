package it.nexoapp.common.configuration.coverter;

import it.nexoapp.common.constant.Environment;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.Optional;

/**
 * MicroProfile Config converter that resolves the application {@link Environment}.
 * A non-blank config value ({@code app.environment}) is used as-is (case-insensitive);
 * otherwise the environment is derived from the first token of the active
 * {@code quarkus.profile}. If neither is set, an {@link IllegalArgumentException} is thrown.
 */
public class EnvironmentConverter implements Converter<Environment> {

    @Override
    public Environment convert(String value) throws IllegalArgumentException, NullPointerException {
        /* manual set */
        if(StringUtils.isNotBlank(value)) {
            return Environment.valueOf(value.toUpperCase());
        }

        /* env based on active quarkus profile */
        final Optional<String> quarkusProfile = ConfigProvider
                .getConfig()
                .getOptionalValue("quarkus.profile", String.class);

        if(quarkusProfile.isPresent()) {
            final String[] split = quarkusProfile.get().split(",");
            return Environment.valueOf(
                    split[0].toUpperCase()
            );
        }

        throw new IllegalArgumentException("Unable to resolve the application environment: set it explicitly via 'app.environment' or activate a Quarkus profile via 'quarkus.profile'.");
    }

}
