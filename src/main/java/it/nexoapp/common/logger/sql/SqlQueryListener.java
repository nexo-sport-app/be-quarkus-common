package it.nexoapp.common.logger.sql;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs every SQL statement Hibernate prepares, at DEBUG only.
 * <p>
 * At the default INFO level the statements are not logged: they can carry personal data in their text. DEBUG is
 * switchable at runtime on the {@code it} category. Parameters and elapsed time are left to dedicated tooling
 * (OpenTelemetry).
 */
@PersistenceUnitExtension
public class SqlQueryListener implements StatementInspector {

    /* - logger - */
    /** Logger of this class, below the {@code it} category. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlQueryListener.class);

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String inspect(final String sql) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("sql={}", sql);
        }
        return sql;
    }

}
