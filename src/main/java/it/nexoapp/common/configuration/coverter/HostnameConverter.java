package it.nexoapp.common.configuration.coverter;

import it.nexoapp.common.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.spi.Converter;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MicroProfile Config converter that resolves the application hostname.
 * Resolution order: the config value itself, the {@code HOSTNAME} system property,
 * the local machine hostname via {@link InetAddress}, then {@link Constant#UNKNOWN}.
 */
public class HostnameConverter implements Converter<String> {

    @Override
    public String convert(final String value) throws IllegalArgumentException, NullPointerException {
        if(StringUtils.isNotBlank(value)) {
            return value;
        }

        final String hostname = System.getProperty("HOSTNAME");
        if(StringUtils.isNotBlank(hostname)) {
            return hostname;
        }

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException _) {
            // ignored, we will use default
        }

        return Constant.UNKNOWN;
    }

}