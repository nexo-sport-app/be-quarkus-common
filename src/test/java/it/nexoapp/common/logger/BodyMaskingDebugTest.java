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

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
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
     * Returns the formatted messages of the records whose text matches a prefix.
     *
     * @param prefix start of the message
     * @return the matching messages
     */
    private List<String> messagesStartingWith(final String prefix) {
        return capture.records().stream().map(LogCapture::message).filter(m -> m != null && m.startsWith(prefix)).toList();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The request and the response body are logged with the sensitive members masked, also in nested records.
     */
    @Test
    void jsonBodiesAreLoggedMasked() {
        given().contentType(ContentType.JSON).body(BODY)
                .post("/probe-logging/json").then().statusCode(200);

        final List<String> request = messagesStartingWith("request body=");
        final List<String> response = messagesStartingWith("response body=");
        assertEquals(1, request.size(), "one request body line: " + request);
        assertEquals(1, response.size(), "one response body line: " + response);
        for (final String line : List.of(request.getFirst(), response.getFirst())) {
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
        final List<String> request = messagesStartingWith("request body=");
        assertEquals(List.of("request body=[body omitted: multipart/form-data]"), request);
        assertTrue(messagesStartingWith("response body=").contains("response body=[body omitted: text/plain]"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A plain text response is replaced by a placeholder.
     */
    @Test
    void textIsReplacedByPlaceholder() {
        given().get("/probe-logging/text").then().statusCode(200);

        assertTrue(messagesStartingWith("response body=").contains("response body=[body omitted: text/plain]"));
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
        assertTrue(messagesStartingWith("response body=").contains("response body=[body omitted: application/octet-stream]"),
                messagesStartingWith("response body=").toString());
        for (final String text : capture.fullTexts()) {
            assertFalse(text.contains("stream-secret"), text);
        }
    }

}
