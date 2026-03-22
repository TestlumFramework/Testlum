package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.MigrationConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatasetValidator {

    private final FileSearcher fileSearcher;

    //CHECKSTYLE:OFF
    public void validateDatasetByExtension(final String datasetFileName, final StorageName name) {
        fileSearcher.searchFileFromDataFolder(datasetFileName);
        switch (name) {
            case CLICKHOUSE:
            case POSTGRES:
            case ORACLE:
            case SQLDATABASE:
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
                throw new DefaultFrameworkException(ExceptionMessage.DB_NOT_SUPPORTED, name);
        }
    }

    //CHECKSTYLE:ON

    private boolean checkIfNotExcelOrCsvFIle(final String datasetFileName) {
        return !(datasetFileName.endsWith(MigrationConstant.XLSX_EXTENSION)
                || datasetFileName.endsWith(MigrationConstant.CSV_EXTENSION)
                || datasetFileName.endsWith(MigrationConstant.XLS_EXTENSION));
    }

    private void checkRelationDbDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(MigrationConstant.SQL_EXTENSION)
                && checkIfNotExcelOrCsvFIle(datasetFileName)) {
            throw new DefaultFrameworkException(ExceptionMessage.UNSUPPORTED_MIGRATION_FORMAT,
                    "Relational databases", MigrationConstant.SQL_EXTENSION,
                    MigrationConstant.CSV_EXTENSION + ", " + MigrationConstant.XLS_EXTENSION);
        }
    }

    private void checkMongodbDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(MigrationConstant.JSON_EXTENSION)
                && !datasetFileName.endsWith(MigrationConstant.BSON_EXTENSION)) {
            throw new DefaultFrameworkException(ExceptionMessage.UNSUPPORTED_MIGRATION_FORMAT,
                    "MongoDB", MigrationConstant.JSON_EXTENSION, MigrationConstant.BSON_EXTENSION);
        }
    }

    private void checkRedisDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(MigrationConstant.TXT_EXTENSION)) {
            throw new DefaultFrameworkException(ExceptionMessage.UNSUPPORTED_MIGRATION_FORMAT,
                    "Redis", MigrationConstant.TXT_EXTENSION);
        }
    }

    private void checkDynamoDbDatasetExtension(final String datasetFileName) {
        if (!datasetFileName.endsWith(MigrationConstant.PARTIQL_EXTENSION)) {
            throw new DefaultFrameworkException(ExceptionMessage.UNSUPPORTED_MIGRATION_FORMAT,
                    "DynamoDB", MigrationConstant.PARTIQL_EXTENSION);
        }
    }
}

