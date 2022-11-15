package com.knubisoft.cott.testing.framework.db.sql.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Slf4j
@UtilityClass
public class SqlExtractor {
    private static final String SELECT_DATABASES =
            "SELECT datname FROM pg_database WHERE datistemplate = false;";

    public List<String> extractQueries(final DataSource dataSource,
                                       final String queryToTableName,
                                       final String... args) {
        List<String> queries = new ArrayList<>();
        for (String each : getTableNames(dataSource, queryToTableName)) {
            for (String arg : args) {
                queries.add(format(arg, each));
            }
        }
        return queries;
    }

    public static String getBadSql(final Exception ex, final String query) {
        final int maxWidth = 100;
        final int position = getSqlPositionFromException(ex) - 50;
        return StringUtils.abbreviate(query, position, maxWidth);
    }

    private static int getSqlPositionFromException(final Exception ex) {
        Pattern p = Pattern.compile("[^0-9]+([0-9]+)$");
        Matcher m = p.matcher(ex.getMessage());
        try {
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            log.error("Unable get position from exception");
            throw new DefaultFrameworkException(e);
        }
        return 0;
    }

    private List<String> getTableNames(final DataSource dataSource,
                                       final String queryToTableName) {
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            return runner.query(queryToTableName, new ColumnListHandler<>(1));
        } catch (SQLException e) {
            log.error("Unable get table names");
            throw new DefaultFrameworkException(e);
        }
    }

    public List<String> getAllDatabases(final DataSource dataSource) {
        QueryRunner runner = new QueryRunner(dataSource);
        try {
            return runner.query(SELECT_DATABASES, new ColumnListHandler<>(1));
        } catch (SQLException e) {
            log.error("Unable get table names");
            throw new DefaultFrameworkException(e);
        }
    }
}
