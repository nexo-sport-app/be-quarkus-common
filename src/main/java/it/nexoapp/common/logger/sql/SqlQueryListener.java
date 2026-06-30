package it.nexoapp.common.logger.sql;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import it.nexoapp.common.logger.AbstractLoggingFilter;
import it.nexoapp.common.logger.Log;
import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * A listener implementation that intercepts SQL query execution events and provides
 * logging functionality for executed queries. This class extends {@code AbstractLoggingFilter}
 * <br/>
 * The primary purpose of this listener is to log executed SQL queries, parameters and elapsed should be tracked
 * by dedicated system with OpenTelemetry.
 */
@PersistenceUnitExtension
public class SqlQueryListener extends AbstractLoggingFilter implements StatementInspector {

    @Override
    protected Log.Type getLogType() {
        return Log.Type.SQL;
    }

    @Override
    public String inspect(final String sql) {
        log(
                Log.builder().sql(sql).build()
        );
        return sql;
    }
}