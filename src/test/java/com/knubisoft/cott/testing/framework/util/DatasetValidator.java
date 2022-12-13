package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.StorageName;
import lombok.experimental.UtilityClass;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DB_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.UNSUPPORTED_MIGRATION_FORMAT;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.BSON_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.CSV_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.JSON_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.PARTIQL_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.SQL_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.TXT_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLSX_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLS_EXTENSION;

@UtilityClass
public class DatasetValidator {
    //CHECKSTYLE:OFF
    public void validateDatasetByExtension(final String datasetFileName, final StorageName name) {
        FileSearcher.searchFileFromDataFolder(datasetFileName);
        switch (name) {
            case CLICKHOUSE:
            case POSTGRES:
            case ORACLE:
            case MYSQL:
                checkRelationDbDatasetExtension(datasetFileName);
                break;
            case REDIS:
                checkRedisDatasetExtension(datasetFileName);
                break;
            case MONGODB:
                checkMongodbDatasetExtension(datasetFileName);
                break;
            case DYNAMO:
                checkDynamoDbDatasetExtension(datasetFileName);
                break;
            default:
                throw new DefaultFrameworkException(DB_NOT_SUPPORTED, name);
        }
    }

    //CHECKSTYLE:ON

    private boolean checkIfNotExcelOrCsvFIle(final String datasetFileName) {
        return !(datasetFileName.endsWith(XLSX_EXTENSION) || datasetFileName.endsWith(CSV_EXTENSION)
                || datasetFileName.endsWith(XLS_EXTENSION));
    }

    private void checkRelationDbDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(SQL_EXTENSION) && checkIfNotExcelOrCsvFIle(datasetFileName)) {
            throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, "Relational databases", SQL_EXTENSION,
                    CSV_EXTENSION + ", " + XLS_EXTENSION);
        }
    }

    private void checkMongodbDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(JSON_EXTENSION) && !datasetFileName.endsWith(BSON_EXTENSION)) {
            throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, "MongoDB", JSON_EXTENSION,
                    BSON_EXTENSION);
        }
    }

    private void checkRedisDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(TXT_EXTENSION)) {
            throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, "Redis", TXT_EXTENSION);
        }
    }

    private void checkDynamoDbDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(PARTIQL_EXTENSION)) {
            throw new DefaultFrameworkException(UNSUPPORTED_MIGRATION_FORMAT, "DynamoDB", PARTIQL_EXTENSION);
        }
    }
}

