package it.nexoapp.common.logger;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.slf4j.event.Level;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class Log implements Serializable {

    @Serial
    private static final long serialVersionUID = -5743800353227241648L;

    /**
     * Represents the logging level for the {@code Log} class.
     * This variable determines the severity of logs that will be processed.
     * <br/>
     * By default, the level is set to {@code Level.INFO}, which is commonly
     * used for informational messages that highlight the progress of the application.
     * <br/>
     * The {@code Level} is immutable and cannot be changed after initialization.
     */
    @Builder.Default
    private final Level level = Level.INFO;

    /**
     * The type of operation being logged (e.g., "Server", "SQL").
     * Example: "HTTP"
     */
    private final Type type;

    /**
     * The type of operation being logged (e.g., "Request", "Response", "Fault").
     * Example: "Request"
     */
    private final Phase phase;

    /**
     * The HTTP method used for the request.
     * Example: "GET", "POST", "PUT", "DELETE"
     */
    private final String method;

    /**
     * The endpoint or URL path that was called.
     * Example: "/api/users/123"
     */
    private final String endpoint;

    /**
     * Unique identifier for the client call to track requests.
     * Example: "req-12345-abcde"
     */
    private final String clientCallId;

    /**
     * The HTTP status code returned by the operation.
     * Example: 200, 404, 500
     */
    private final Integer status;

    /**
     * Time elapsed for the operation in milliseconds.
     * Example: 1250L (represents 1.25 seconds)
     */
    private final Long elapsed;

    /**
     * The number of database queries executed during the operation.
     * Example: 3
     */
    private final Integer queryCount;

    /**
     * HTTP headers associated with the request or response.
     * Example: "Content-Type: application/json; Authorization: Bearer token123"
     */
    private final String headers;

    /**
     * The request or response body content.
     * Example: "{\"userId\": 123, \"name\": \"John Doe\"}"
     */
    private final String body;

    /**
     * The SQL query or queries executed during the operation.
     * Example: "SELECT * FROM users WHERE id = ?; UPDATE users SET last_login = NOW()"
     */
    private final String sql;

    /**
     * Represents the type of log entry.
     * Used to categorize logs based on their source or purpose.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum Type {

        /**
         * Enum constant representing the server log type.
         * Defines a category of logs associated with server-related events or operations.
         */
        SERVER,

        /**
         * Enum constant representing the client log type.
         * Defines a category of logs associated with client-related events or operations.
         */
        CLIENT,

        /**
         * Enum constant representing the SQL log type.
         * Defines a category of logs associated with SQL-related events or operations.
         */
        SQL

    }

    /**
     * Represents the phase of an operation in the logging context.
     * Phases are used to describe the stage of an operation or request cycle.
     * <br/>
     * REQUEST - Indicates the request phase of an operation.
     * RESPONSE - Indicates the response phase of an operation.
     * FAULT - Indicates an error or fault phase in an operation.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum Phase {

        /**
         * Represents the request phase of an operation in the logging context.
         * This phase indicates the initiation point or beginning of an operation.
         */
        REQUEST,

        /**
         * Represents the response phase of an operation in the logging context.
         * This phase indicates the completion of an operation where a response is issued.
         */
        RESPONSE,

        /**
         * Represents the fault phase of an operation in the logging context.
         * This phase indicates an error or fault has occurred during an operation.
         */
        FAULT

    }

}