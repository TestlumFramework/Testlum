package com.knubisoft.testlum.testing.framework.db.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@UtilityClass
public class ResultUtil {

    private static final String DATABASE = "Database";
    private static final String PATCHES = "Patches";
    private static final String QUERIES = "Queries";
    private static final String DATABASE_ALIAS = "Database alias";

    public void addMigrateMetaData(final String databaseName,
                                   final String databaseAlias,
                                   final List<String> patches,
                                   final CommandResult result) {
        result.put(DATABASE, databaseName);
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(PATCHES, patches);
    }

    public void addDatabaseMetaData(final String databaseAlias,
                                    final List<String> queries,
                                    final CommandResult result) {
        result.put(DATABASE_ALIAS, databaseAlias);
        result.put(QUERIES, queries);
    }
}
