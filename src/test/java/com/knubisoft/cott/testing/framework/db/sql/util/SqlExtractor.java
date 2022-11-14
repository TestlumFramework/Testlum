package com.knubisoft.cott.testing.framework.db.sql.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

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
    private static final String SPACE_PLUS = " +";
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

    public String getBrokenQuery(final Exception ex, final String query) {
        int position = getQueryPositionFromException(ex) - 1;
        int startOfLine = startOfQueryLine(position, query);
        int endOfLine = endOfQueryLine(position, query);
        return query.substring(startOfLine, endOfLine);
    }

    private static int endOfQueryLine(final int position, final String query) {
        for (int i = position; i < query.length(); i++) {
            if (query.charAt(i) == '\r') {
                return i;
            } else if (i == query.length() - 1) {
                return i + 1;
            }
        }
        return position;
    }

    private static int startOfQueryLine(final int position, final String query) {
        for (int i = position; i >= 0; i--) {
            if (query.charAt(i) == '\r') {
                return i + 1;
            } else if (i == 0) {
                return i;
            }
        }
        return position;
    }

    private static int getQueryPositionFromException(final Exception ex) {
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
        return -1;
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
