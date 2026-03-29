package com.knubisoft.testlum.testing.framework.db.source;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileSourceTest {

    @TempDir
    File tempDir;

    @Nested
    class PlainTextFile {
        @Test
        void singleQueryFromTextFile() throws IOException {
            File file = createFile("test.sql", "SELECT 1");
            FileSource source = new FileSource(file);
            assertEquals(1, source.getQueries().size());
            assertEquals("SELECT 1", source.getQueries().get(0));
        }

        @Test
        void multipleQueriesSplitByDelimiter() throws IOException {
            File file = createFile("test.sql", "SELECT 1;;SELECT 2;;SELECT 3");
            FileSource source = new FileSource(file);
            assertEquals(3, source.getQueries().size());
            assertEquals("SELECT 1", source.getQueries().get(0));
            assertEquals("SELECT 2", source.getQueries().get(1));
            assertEquals("SELECT 3", source.getQueries().get(2));
        }

        @Test
        void blankQueriesFilteredFromTextFile() throws IOException {
            File file = createFile("test.sql", "SELECT 1;;  ;;SELECT 2");
            FileSource source = new FileSource(file);
            assertEquals(2, source.getQueries().size());
        }

        @Test
        void newlinesReplacedWithSpaces() throws IOException {
            File file = createFile("test.sql", "SELECT\n1\nFROM\ntable1");
            FileSource source = new FileSource(file);
            assertEquals("SELECT 1 FROM table1", source.getQueries().get(0));
        }

        @Test
        void emptyFileProducesNoQueries() throws IOException {
            File file = createFile("test.txt", "");
            FileSource source = new FileSource(file);
            assertTrue(source.getQueries().isEmpty());
        }

        @Test
        void txtExtensionTreatedAsPlainText() throws IOException {
            File file = createFile("data.txt", "INSERT INTO t VALUES(1)");
            FileSource source = new FileSource(file);
            assertEquals(1, source.getQueries().size());
        }

        @Test
        void jsonExtensionTreatedAsPlainText() throws IOException {
            File file = createFile("data.json", "SELECT * FROM users");
            FileSource source = new FileSource(file);
            assertEquals(1, source.getQueries().size());
        }
    }

    @Nested
    class CsvFile {
        @Test
        void csvFileDelegatesToCsvParser() throws IOException {
            File file = createFile("test.csv", "users,John,30");
            FileSource source = new FileSource(file);
            assertFalse(source.getQueries().isEmpty());
        }

        @Test
        void csvFileProducesInsertStatements() throws IOException {
            File file = createFile("data.csv", "products,Widget,9.99\nproducts,Gadget,19.99");
            FileSource source = new FileSource(file);
            assertEquals(2, source.getQueries().size());
        }
    }

    @Nested
    class ExcelFile {
        @Test
        void nonExistentXlsxThrowsException() {
            File file = new File(tempDir, "nonexistent.xlsx");
            assertThrows(RuntimeException.class, () -> new FileSource(file));
        }

        @Test
        void nonExistentXlsThrowsException() {
            File file = new File(tempDir, "nonexistent.xls");
            assertThrows(RuntimeException.class, () -> new FileSource(file));
        }
    }

    @Nested
    class ErrorHandling {
        @Test
        void nonExistentFileCausesException() {
            File file = new File(tempDir, "does_not_exist.sql");
            assertThrows(DefaultFrameworkException.class, () -> new FileSource(file));
        }
    }

    private File createFile(final String name, final String content) throws IOException {
        File file = new File(tempDir, name);
        Files.writeString(file.toPath(), content);
        return file;
    }
}
