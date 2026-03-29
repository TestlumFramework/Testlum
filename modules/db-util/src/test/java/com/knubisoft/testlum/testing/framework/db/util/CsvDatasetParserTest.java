package com.knubisoft.testlum.testing.framework.db.util;

import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvDatasetParser} verifying CSV-to-SQL INSERT
 * conversion, table name extraction, and value parsing.
 */
class CsvDatasetParserTest {

    @TempDir
    File tempDir;

    private File createCsvFile(final String content) throws IOException {
        final File csv = new File(tempDir, "test.csv");
        Files.writeString(csv.toPath(), content);
        return csv;
    }

    @Nested
    class ValidCsv {
        @Test
        void singleRowProducesOneInsert() throws IOException {
            final File csv = createCsvFile("users,John,30");
            final ListSource source = CsvDatasetParser.getSource(csv);
            assertFalse(source.getQueries().isEmpty());
            assertEquals(1, source.getQueries().size());
        }

        @Test
        void multipleRowsProduceMultipleInserts() throws IOException {
            final File csv = createCsvFile("users,John,30\nusers,Jane,25");
            final ListSource source = CsvDatasetParser.getSource(csv);
            assertEquals(2, source.getQueries().size());
        }
    }

    @Nested
    class InvalidCsv {
        @Test
        void missingTableNameThrows() throws IOException {
            final File csv = createCsvFile(",value1,value2");
            assertThrows(IllegalArgumentException.class,
                    () -> CsvDatasetParser.getSource(csv));
        }

        @Test
        void onlyTableNameThrows() throws IOException {
            final File csv = createCsvFile("tablename,");
            // getValues will get empty string after comma
            assertThrows(IllegalArgumentException.class,
                    () -> CsvDatasetParser.getSource(csv));
        }

        @Test
        void nonExistentFileThrowsRuntimeException() {
            final File csv = new File(tempDir, "nonexistent.csv");
            assertThrows(RuntimeException.class,
                    () -> CsvDatasetParser.getSource(csv));
        }
    }

    @Nested
    class SqlFormat {
        @Test
        void generatedQueryContainsInsertInto() throws IOException {
            final File csv = createCsvFile("users,John,30");
            final ListSource source = CsvDatasetParser.getSource(csv);
            assertTrue(source.getQueries().get(0).contains("INSERT INTO"));
        }

        @Test
        void generatedQueryContainsTableName() throws IOException {
            final File csv = createCsvFile("my_table,val1,val2");
            final ListSource source = CsvDatasetParser.getSource(csv);
            assertTrue(source.getQueries().get(0).contains("my_table"));
        }

        @Test
        void generatedQueryContainsValues() throws IOException {
            final File csv = createCsvFile("users,John,30");
            final ListSource source = CsvDatasetParser.getSource(csv);
            final String query = source.getQueries().get(0);
            assertTrue(query.contains("John"));
            assertTrue(query.contains("30"));
        }

        @Test
        void generatedQueryMatchesInsertFormat() throws IOException {
            final File csv = createCsvFile("users,John,30");
            final ListSource source = CsvDatasetParser.getSource(csv);
            final String query = source.getQueries().get(0);
            assertTrue(query.startsWith("INSERT INTO users VALUES (John,30);"));
        }
    }
}
