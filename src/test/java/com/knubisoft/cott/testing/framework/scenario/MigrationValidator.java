package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.model.scenario.StorageName;
import lombok.experimental.UtilityClass;

import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.BSON_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.CSV_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.JSON_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.PARTIQL_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.SQL_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLSX_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLS_EXTENSION;
import static com.knubisoft.cott.testing.framework.util.LogMessage.UNSUPPORTED_MIGRATION_FORMAT;

@UtilityClass
public class MigrationValidator {
    //CHECKSTYLE:OFF
    public void validateMigrationByExtension(final String patch, final StorageName name) {
        FileSearcher.searchFileFromDataFolder(patch);
        boolean commonExtensions = patch.endsWith(XLSX_EXTENSION) || patch.endsWith(CSV_EXTENSION)
                || patch.endsWith(XLS_EXTENSION);
        switch (name) {
            case CLICKHOUSE:
            case POSTGRES:
            case ORACLE:
            case MYSQL:
                if (!patch.endsWith(SQL_EXTENSION) && !commonExtensions) {
                    throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, name, SQL_EXTENSION, CSV_EXTENSION
                            + ", " + XLS_EXTENSION);
                }
                break;
            case REDIS:
                if (commonExtensions) {
                    throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, name, ".txt");
                }
                break;
            case MONGODB:
                if (!patch.endsWith(JSON_EXTENSION) && !patch.endsWith(BSON_EXTENSION)) {
                    throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, name, JSON_EXTENSION,
                            BSON_EXTENSION);
                }
                break;
            case DYNAMO:
                if (!patch.endsWith(PARTIQL_EXTENSION)) {
                    throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, name, PARTIQL_EXTENSION);
                }
                default:
                    break;
        }
    }
}

