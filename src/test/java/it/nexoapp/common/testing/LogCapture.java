package it.nexoapp.common.testing;

import org.jboss.logmanager.ExtLogRecord;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Collects the log records published while it is installed, published in the test-jar of the common library.
 * <p>
 * Usage: create one, call {@link #install()} before the action under test and {@link #uninstall()} after it
 * (typically in {@code @BeforeEach} and {@code @AfterEach}). The handler is attached to the root logger and also
 * to the platform categories that do not forward to their parent ({@code it}, {@code EXCEPTION}, {@code ACCESS}),
 * so records of every logger of the application are seen exactly once.
 * <p>
 * Only records that pass the level of their logger are captured: raise a logger's level to see its DEBUG lines.
 */
public final class LogCapture {

    /* - constants - */
    /** Loggers a handler is attached to: the root and the categories configured not to use parent handlers. */
    private static final List<String> CAPTURE_POINTS = List.of("", "it", "EXCEPTION", "ACCESS");

    /* - state - */
    /** Records collected so far, each instance once. */
    private final Set<LogRecord> collected = Collections.newSetFromMap(Collections.synchronizedMap(new IdentityHashMap<>()));

    /** Records in publication order. */
    private final List<LogRecord> ordered = Collections.synchronizedList(new ArrayList<>());

    /** Handler attached to the capture points. */
    private final Handler handler = new Handler() {
        @Override
        public void publish(final LogRecord record) {
            if (collected.add(record)) {
                ordered.add(record);
            }
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
     * Starts collecting; previously collected records are dropped.
     */
    public void install() {
        clear();
        handler.setLevel(Level.ALL);
        for (final String name : CAPTURE_POINTS) {
            Logger.getLogger(name).addHandler(handler);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Stops collecting; the records already collected stay readable.
     */
    public void uninstall() {
        for (final String name : CAPTURE_POINTS) {
            Logger.getLogger(name).removeHandler(handler);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Drops the records collected so far.
     */
    public void clear() {
        ordered.clear();
        collected.clear();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns every collected record.
     *
     * @return a snapshot of the records, in publication order
     */
    public List<LogRecord> records() {
        synchronized (ordered) {
            return List.copyOf(ordered);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the records published on one named logger.
     *
     * @param loggerName exact logger name, for example {@code ACCESS}
     * @return a snapshot of the matching records, in publication order
     */
    public List<LogRecord> recordsOf(final String loggerName) {
        return records().stream().filter(r -> loggerName.equals(r.getLoggerName())).toList();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the formatted messages of the records at or above a level.
     *
     * @param level lowest level to include
     * @return the formatted messages, in publication order
     */
    public List<String> messagesAtOrAbove(final Level level) {
        return records().stream()
                .filter(r -> r.getLevel().intValue() >= level.intValue())
                .map(LogCapture::message)
                .toList();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns the complete text of every record, for secret-absence assertions: the formatted message, the raw
     * message, every parameter and the stack trace summary.
     *
     * @return one text per record, in publication order
     */
    public List<String> fullTexts() {
        return records().stream().map(LogCapture::fullText).toList();
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Formats the message of a record, filling its placeholders.
     *
     * @param record the record
     * @return the formatted message
     */
    public static String message(final LogRecord record) {
        if (record instanceof ExtLogRecord ext) {
            return ext.getFormattedMessage();
        }
        final String raw = record.getMessage();
        final Object[] params = record.getParameters();
        return raw == null || params == null || params.length == 0 ? raw : MessageFormat.format(raw, params);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Builds the complete searchable text of a record.
     *
     * @param record the record
     * @return the text
     */
    private static String fullText(final LogRecord record) {
        final StringBuilder text = new StringBuilder();
        text.append(message(record)).append('\n').append(record.getMessage());
        if (record.getParameters() != null) {
            for (final Object parameter : record.getParameters()) {
                text.append('\n').append(parameter);
            }
        }
        if (record.getThrown() != null) {
            text.append('\n').append(record.getThrown());
        }
        return text.toString();
    }

}
