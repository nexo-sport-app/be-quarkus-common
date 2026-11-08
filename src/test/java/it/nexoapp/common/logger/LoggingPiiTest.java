package it.nexoapp.common.logger;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import it.nexoapp.common.constant.LoggerName;
import it.nexoapp.common.tenant.TenantContext;
import it.nexoapp.common.tenant.probe.ProbeRepository;
import it.nexoapp.common.testing.LogCapture;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * At the default level one request leaves exactly one access line and no log record of any logger carries a body,
 * a credential, a bearer token or personal data (FND-03, D-17, D-19).
 */
@QuarkusTest
@TestSecurity(user = "probe")
class LoggingPiiTest {

    /* - constants - */
    /** Body of the probe request: personal data at top level and a secret inside a nested record. */
    private static final String BODY = """
            {"name":"visible-name","taxCode":"12345678901","items":[{"a":"plain","secret":"nested-secret-1"}]}""";

    /** Literals that must never appear in any record. */
    private static final List<String> SECRETS = List.of(
            "secret-token-1", "secret-token-2", "secret-cookie", "12345678901", "nested-secret-1", "Bearer", "visible-name");

    /* - fixtures - */
    /** Captures the records of the test. */
    private final LogCapture capture = new LogCapture();

    /* - repository - */
    /** Probe repository, used to run SQL. */
    @Inject
    ProbeRepository probeRepository;

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
     * Sends the probe request with credentials in several header spellings.
     *
     * @return the response body
     */
    private String postProbe() {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer secret-token-1")
                .header("aUtHoRiZaTiOn", "Bearer secret-token-2")
                .header("Cookie", "session=secret-cookie")
                .header("X-Call-Id", "call-pii-1")
                .body(BODY)
                .post("/probe-logging/json?taxCode=12345678901")
                .then().statusCode(200)
                .extract().asString();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * One request yields exactly one record on the access logger with method, path, status, duration and call id.
     */
    @Test
    void oneAccessLinePerRequest() {
        postProbe();

        final List<LogRecord> access = capture.recordsOf(LoggerName.ACCESS);
        assertEquals(1, access.size(), "one access line: " + access.stream().map(LogCapture::message).toList());
        final LogRecord record = access.getFirst();
        assertEquals(Level.INFO.intValue(), record.getLevel().intValue());
        final String line = LogCapture.message(record);
        assertTrue(line.contains("method=POST"), line);
        assertTrue(line.contains("path=/nexo-cmn/probe-logging/json"), line);
        assertTrue(line.contains("status=200"), line);
        assertTrue(line.contains("durationMs="), line);
        assertTrue(line.contains("callId=call-pii-1"), line);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A request that no resource handles still yields one access line, with its status.
     */
    @Test
    void routingErrorYieldsOneAccessLine() {
        given().get("/no-such-path").then().statusCode(404);

        final List<LogRecord> access = capture.recordsOf(LoggerName.ACCESS);
        assertEquals(1, access.size(), access.stream().map(LogCapture::message).toList().toString());
        assertTrue(LogCapture.message(access.getFirst()).contains("status=404"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The access line has no query string, so a value passed there is not logged.
     */
    @Test
    void accessLineHasNoQueryString() {
        postProbe();

        final String line = LogCapture.message(capture.recordsOf(LoggerName.ACCESS).getFirst());
        assertFalse(line.contains("?"), line);
        assertFalse(line.contains("taxCode"), line);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A call id with control characters cannot forge a second log line.
     */
    @Test
    void callIdCannotInjectLines() {
        given().header("X-Call-Id", "x\ninjected").get("/no-such-path").then().statusCode(404);

        final String line = LogCapture.message(capture.recordsOf(LoggerName.ACCESS).getFirst());
        assertFalse(line.contains("\n"), line);
        assertFalse(line.contains("\r"), line);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * No record of any logger, at any level, holds a body, a header value or a token; the request body is not
     * logged and neither is the response body.
     */
    @Test
    void noSecretInAnyRecord() {
        final String response = postProbe();
        assertTrue(response.contains("12345678901"), "the probe echoes the request");

        final List<String> texts = capture.fullTexts();
        assertFalse(texts.isEmpty(), "the access line is captured");
        for (final String text : texts) {
            for (final String secret : SECRETS) {
                assertFalse(text.contains(secret), "record leaks '" + secret + "': " + text);
            }
            assertFalse(text.contains(BODY), "record holds the body: " + text);
            assertFalse(text.contains("\"items\""), "record holds a JSON body: " + text);
            assertFalse(text.toLowerCase().contains("authorization"), "record names the Authorization header: " + text);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The body logging interceptor emits nothing at the default level.
     */
    @Test
    void bodyInterceptorIsSilentAtInfo() {
        postProbe();

        assertTrue(capture.records().stream()
                        .noneMatch(r -> "it.nexoapp.common.logger.BodyLoggingInterceptor".equals(r.getLoggerName())),
                "the interceptor must not log at INFO");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * SQL statements leave no record at the default level.
     */
    @Test
    void sqlIsNotLoggedAtInfo() {
        runQuery();

        for (final LogRecord record : capture.records()) {
            final String text = LogCapture.message(record).toLowerCase();
            assertFalse(text.contains("select ") || text.contains("insert "), "SQL at INFO: " + text);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * DEBUG stays switchable at runtime: raising the level of the SQL logger makes the statements appear.
     */
    @Test
    void sqlAppearsWhenDebugIsSwitchedOn() {
        final Logger sqlLogger = Logger.getLogger("it.nexoapp.common.logger.sql");
        final Level before = sqlLogger.getLevel();
        sqlLogger.setLevel(Level.FINE);
        try {
            runQuery();
        } finally {
            sqlLogger.setLevel(before);
        }

        final boolean seen = capture.records().stream()
                .anyMatch(r -> LogCapture.message(r).toLowerCase().contains("select "));
        assertTrue(seen, "DEBUG SQL line expected once the level is raised");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Runs one query against the probe table in a tenant scope.
     */
    private void runQuery() {
        TenantContext.callAs(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                () -> QuarkusTransaction.requiringNew().call(() -> probeRepository.count()));
    }

}
