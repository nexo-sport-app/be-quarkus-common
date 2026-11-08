package it.nexoapp.common.exception;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import it.nexoapp.common.constant.LoggerName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Client errors (4xx) are logged as one WARN line without stack trace; server errors keep ERROR with the stack
 * trace.
 */
@QuarkusTest
@TestSecurity(user = "probe")
class ClientErrorLoggingTest {

    /** Records published on the exception logger during a test. */
    private final List<LogRecord> records = new CopyOnWriteArrayList<>();

    /** Handler collecting the records. */
    private final Handler handler = new Handler() {
        @Override
        public void publish(final LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
            // nothing buffered
        }

        @Override
        public void close() {
            // nothing to release
        }
    };

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Starts collecting the records of the exception logger.
     */
    @BeforeEach
    void attach() {
        records.clear();
        Logger.getLogger(LoggerName.EXCEPTION).addHandler(handler);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Stops collecting.
     */
    @AfterEach
    void detach() {
        Logger.getLogger(LoggerName.EXCEPTION).removeHandler(handler);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Asserts that exactly one record was logged, at WARN and without a stack trace, naming code and call id.
     *
     * @param code the fault code the line must mention
     */
    private void assertSingleWarn(final String code) {
        assertEquals(1, records.size(), "one line per client error: " + records);
        final LogRecord record = records.getFirst();
        assertEquals(Level.WARNING.intValue(), record.getLevel().intValue());
        assertNull(record.getThrown(), "no stack trace for a client error");
        final String line = format(record);
        assertTrue(line.contains(code), line);
        assertTrue(line.contains("callId="), line);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Formats a record the way slf4j's placeholders were filled.
     *
     * @param record the record
     * @return the formatted message
     */
    private static String format(final LogRecord record) {
        final org.jboss.logmanager.ExtLogRecord ext = (org.jboss.logmanager.ExtLogRecord) record;
        return ext.getFormattedMessage();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A validation failure is a WARN line.
     */
    @Test
    void validationFailureIsOneWarnLine() {
        given().contentType(ContentType.JSON).body("{}").post("/probe-validation").then().statusCode(400);
        assertSingleWarn("nexo.validation");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A routing error is a WARN line.
     */
    @Test
    void routingErrorIsOneWarnLine() {
        given().get("/no-such-path").then().statusCode(404);
        assertSingleWarn("nexo.http.404");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The identity gate's 403 (an application exception) is a WARN line.
     */
    @Test
    void identityGateDenialIsOneWarnLine() {
        given().get("/probe-security/authenticated").then().statusCode(403);
        assertSingleWarn("IDENTITY_NOT_LINKED");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An unexpected failure keeps ERROR with the stack trace.
     */
    @Test
    void serverErrorKeepsErrorWithStackTrace() {
        given().get("/probe-validation/boom").then().statusCode(500);
        final LogRecord record = records.stream().filter(r -> r.getLevel().intValue() == Level.SEVERE.intValue()).findFirst().orElse(null);
        assertNotNull(record, "a server error is logged at ERROR: " + records);
        assertNotNull(record.getThrown(), "the stack trace is kept");
    }

}
