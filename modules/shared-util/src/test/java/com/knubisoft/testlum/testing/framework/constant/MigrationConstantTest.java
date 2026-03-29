package com.knubisoft.testlum.testing.framework.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MigrationConstantTest {

    @Test
    void verifyAllConstants() {
        assertEquals("INSERT INTO %s VALUES (%s);", MigrationConstant.SQL_INSERT);
        assertEquals(".xlsx", MigrationConstant.XLSX_EXTENSION);
        assertEquals(".xls", MigrationConstant.XLS_EXTENSION);
        assertEquals(".csv", MigrationConstant.CSV_EXTENSION);
        assertEquals(".json", MigrationConstant.JSON_EXTENSION);
        assertEquals(".bson", MigrationConstant.BSON_EXTENSION);
        assertEquals(".txt", MigrationConstant.TXT_EXTENSION);
        assertEquals(".partiql", MigrationConstant.PARTIQL_EXTENSION);
        assertEquals(".sql", MigrationConstant.SQL_EXTENSION);
    }

    @Test
    void sqlInsertFormatsCorrectly() {
        String result = String.format(MigrationConstant.SQL_INSERT, "users", "'John', 25");
        assertEquals("INSERT INTO users VALUES ('John', 25);", result);
    }
}
