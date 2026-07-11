package it.nexoapp.common.configuration.coverter;

import it.nexoapp.common.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HostnameConverterTest {

    private static final String HOSTNAME = "HOSTNAME";

    private final HostnameConverter converter = new HostnameConverter();

    private String originalHostname;

    @BeforeEach
    void setUp() {
        originalHostname = System.getProperty(HOSTNAME);
        System.clearProperty(HOSTNAME);
    }

    @AfterEach
    void tearDown() {
        if (originalHostname == null) {
            System.clearProperty(HOSTNAME);
        } else {
            System.setProperty(HOSTNAME, originalHostname);
        }
    }

    @Test
    void convertNonBlankValueIsReturnedAsIs() {
        assertEquals("my-host", converter.convert("my-host"), "A non-blank value should be returned as-is");
    }

    @Test
    void convertNonBlankValueWinsOverSystemProperty() {
        System.setProperty(HOSTNAME, "should-not-be-used");
        assertEquals("my-host", converter.convert("my-host"), "The manual value should have priority over the HOSTNAME system property");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void convertBlankValueUsesSystemProperty(final String value) {
        System.setProperty(HOSTNAME, "pod-1234");
        assertEquals("pod-1234", converter.convert(value), "A blank value should fall back to the HOSTNAME system property");
    }

    @Test
    void convertBlankValueWithBlankSystemPropertyFallsBackToLocalHostname() {
        System.setProperty(HOSTNAME, "   ");
        assertEquals(localHostnameOrUnknown(), converter.convert(null), "A blank HOSTNAME system property should be ignored");
    }

    @Test
    void convertBlankValueFallsBackToLocalHostname() {
        assertEquals(localHostnameOrUnknown(), converter.convert(null), "Without HOSTNAME the local machine hostname should be used");
    }

    @Test
    void convertNeverReturnsBlank() {
        assertTrue(StringUtils.isNotBlank(converter.convert(null)), "The converter should always resolve a non-blank hostname");
    }

    /* same resolution the converter performs as last steps: local hostname, otherwise the unknown constant */
    private String localHostnameOrUnknown() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException _) {
            return Constant.UNKNOWN;
        }
    }

}