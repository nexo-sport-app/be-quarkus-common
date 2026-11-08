package it.nexoapp.common.logger;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import it.nexoapp.common.testing.LogCapture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With the application loggers at DEBUG, JSON record bodies are logged with every {@link Sensitive} member masked and
 * every other body is replaced by a placeholder (D-18, T-01-50).
 */
@QuarkusTest
@TestProfile(BodyMaskingDebugTest.DebugProfile.class)
@TestSecurity(user = "probe")
class BodyMaskingDebugTest {

    /* - constants - */
    /** Body of the probe request: personal data at top level and a secret inside a nested record. */
    private static final String BODY = """
            {"name":"visible-name","taxCode":"12345678901","items":[{"a":"plain","secret":"nested-secret-1"}]}""";

    /** Logger name of the body logging interceptor. */
    private static final String INTERCEPTOR = "it.nexoapp.common.logger.BodyLoggingInterceptor";

    /** Logger name of the outbound client filter. */
    private static final String CLIENT = "it.nexoapp.common.logger.ClientLoggingFilter";

    /* - fixtures - */
    /** Captures the records of the test. */
    private final LogCapture capture = new LogCapture();

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Profile that switches the application loggers to DEBUG.
     */
    public static class DebugProfile implements QuarkusTestProfile {

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("app.log.level", "DEBUG");
        }

    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Starts capturing before the request under test.
     */
    @BeforeEach
    void attach() {
        capture.install();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Stops capturing.
     */
    @AfterEach
    void detach() {
        capture.uninstall();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the formatted messages of the records of one logger whose text starts with a prefix.
     *
     * @param logger the exact logger name
     * @param prefix start of the message
     * @return the matching messages
     */
    private List<String> messages(final String logger, final String prefix) {
        return capture.recordsOf(logger).stream().map(LogCapture::message)
                .filter(m -> m != null && m.startsWith(prefix)).toList();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the request lines written by the body logging interceptor.
     *
     * @return the formatted messages
     */
    private List<String> requestLines() {
        return messages(INTERCEPTOR, "request headers=");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the response lines written by the body logging interceptor.
     *
     * @return the formatted messages
     */
    private List<String> responseLines() {
        return messages(INTERCEPTOR, "response headers=");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Extracts the body part of an interceptor line.
     *
     * @param line the formatted message
     * @return the text after {@code body=}
     */
    private static String bodyOf(final String line) {
        return line.substring(line.indexOf(" body=") + " body=".length());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The request and the response body are logged with the sensitive members masked, also in nested records.
     */
    @Test
    void jsonBodiesAreLoggedMasked() {
        given().contentType(ContentType.JSON).body(BODY)
                .post("/probe-logging/json").then().statusCode(200);

        final List<String> request = requestLines();
        final List<String> response = responseLines();
        assertEquals(1, request.size(), "one request body line: " + request);
        assertEquals(1, response.size(), "one response body line: " + response);
        for (final String line : List.of(bodyOf(request.getFirst()), bodyOf(response.getFirst()))) {
            assertTrue(line.contains("\"taxCode\":\"##\""), line);
            assertTrue(line.contains("\"secret\":\"##\""), line);
            assertTrue(line.contains("\"name\":\"visible-name\""), line);
            assertFalse(line.contains("12345678901"), line);
            assertFalse(line.contains("nested-secret-1"), line);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A multipart request is never logged raw: its content appears in no record.
     */
    @Test
    void multipartIsNotLogged() {
        given().multiPart("field", "multipart-field-secret")
                .multiPart("file", "doc.txt", "multipart-file-secret".getBytes(), "text/plain")
                .post("/probe-logging/multipart").then().statusCode(200);

        for (final String text : capture.fullTexts()) {
            assertFalse(text.contains("multipart-field-secret"), text);
            assertFalse(text.contains("multipart-file-secret"), text);
        }
        final List<String> request = requestLines();
        assertEquals(List.of("[body omitted: multipart/form-data]"), request.stream().map(BodyMaskingDebugTest::bodyOf).toList());
        assertTrue(responseLines().stream().map(BodyMaskingDebugTest::bodyOf).anyMatch("[body omitted: text/plain]"::equals));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A plain text response is replaced by a placeholder.
     */
    @Test
    void textIsReplacedByPlaceholder() {
        given().get("/probe-logging/text").then().statusCode(200);

        assertTrue(responseLines().stream().map(BodyMaskingDebugTest::bodyOf).anyMatch("[body omitted: text/plain]"::equals));
        for (final String text : capture.fullTexts()) {
            assertFalse(text.contains("plain-text-secret"), text);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A stream response is replaced by a placeholder and the stream is not consumed by the logger.
     */
    @Test
    void streamIsReplacedByPlaceholder() {
        final String body = given().get("/probe-logging/stream").then().statusCode(200).extract().asString();

        assertEquals("stream-secret", body);
        assertTrue(responseLines().stream().map(BodyMaskingDebugTest::bodyOf).anyMatch("[body omitted: application/octet-stream]"::equals),
                responseLines().toString());
        for (final String text : capture.fullTexts()) {
            assertFalse(text.contains("stream-secret"), text);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Only allowlisted headers are logged, whatever the letter case of the credential headers, and their values
     * appear nowhere.
     */
    @Test
    void headersAreLoggedByAllowlistOnly() {
        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer secret-token-1")
                .header("aUtHoRiZaTiOn", "Bearer secret-token-2")
                .header("Cookie", "session=secret-cookie")
                .header("X-Unknown", "unknown-value")
                .header("X-Call-Id", "call-hdr-1")
                .body(BODY)
                .post("/probe-logging/json").then().statusCode(200);

        final String line = requestLines().getFirst();
        assertTrue(line.contains("content-type"), line);
        assertTrue(line.contains("x-call-id=[call-hdr-1]"), line);
        for (final String text : capture.fullTexts()) {
            assertFalse(text.contains("secret-token-1"), text);
            assertFalse(text.contains("secret-token-2"), text);
            assertFalse(text.contains("secret-cookie"), text);
            assertFalse(text.contains("unknown-value"), text);
            assertFalse(text.toLowerCase().contains("authorization"), text);
            assertFalse(text.toLowerCase().contains("cookie"), text);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The allowlist keeps the listed headers, ignores the letter case and drops everything else.
     */
    @Test
    void allowlistFiltersCaseInsensitively() {
        final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("AUTHORIZATION", "Bearer x");
        headers.add("Content-Type", "application/json");
        headers.add("X-Unknown", "y");
        headers.add("X-ASSOCIATION-ID", "a\nb");

        final Map<String, List<String>> kept = HeaderAllowlist.filter(headers);

        assertEquals(Set.of("content-type", "x-association-id"), kept.keySet());
        assertEquals(List.of("application/json"), kept.get("content-type"));
        assertEquals(List.of("a_b"), kept.get("x-association-id"));
        assertTrue(HeaderAllowlist.filter(null).isEmpty());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An outbound call yields one DEBUG line with method, URI without query string, status, duration and
     * allowlisted headers, and no body.
     */
    @Test
    void outboundCallIsLoggedOnce() {
        given().get("/probe-logging/call").then().statusCode(200).body(equalTo("status 200"));

        final List<String> lines = messages(CLIENT, "client ");
        assertEquals(1, lines.size(), lines.toString());
        final String line = lines.getFirst();
        assertTrue(line.contains("method=GET"), line);
        assertTrue(line.contains("/nexo-cmn/probe-logging/text "), line);
        assertTrue(line.contains("status=200"), line);
        assertTrue(line.contains("durationMs="), line);
        assertTrue(line.contains("x-call-id=[client-call-1]"), line);
        assertFalse(line.contains("?"), line);
        assertFalse(line.contains("query-secret"), line);
        assertFalse(line.contains("plain-text-secret"), line);
        for (final String text : capture.recordsOf(CLIENT).stream().map(LogCapture::message).toList()) {
            assertFalse(text.toLowerCase().contains("authorization"), text);
            assertFalse(text.contains("client-secret"), text);
        }
    }

}
