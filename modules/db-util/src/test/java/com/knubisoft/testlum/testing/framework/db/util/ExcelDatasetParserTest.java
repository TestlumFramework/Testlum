package com.knubisoft.testlum.testing.framework.db.util;

import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ExcelDatasetParserTest {

    @TempDir
    File tempDir;

    @Nested
    class XlsFormat {
        @Test
        void singleRowProducesOneInsert() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("users");
                row.createCell(1).setCellValue("John");
                row.createCell(2).setCellValue("Doe");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertEquals(1, source.getQueries().size());
        }

        @Test
        void multipleRowsProduceMultipleInserts() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row1 = sheet.createRow(0);
                row1.createCell(0).setCellValue("users");
                row1.createCell(1).setCellValue("John");
                Row row2 = sheet.createRow(1);
                row2.createCell(0).setCellValue("users");
                row2.createCell(1).setCellValue("Jane");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertEquals(2, source.getQueries().size());
        }

        @Test
        void multipleSheetsAllProcessed() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet1 = wb.createSheet("Sheet1");
                Row row1 = sheet1.createRow(0);
                row1.createCell(0).setCellValue("users");
                row1.createCell(1).setCellValue("John");

                Sheet sheet2 = wb.createSheet("Sheet2");
                Row row2 = sheet2.createRow(0);
                row2.createCell(0).setCellValue("orders");
                row2.createCell(1).setCellValue("100");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertEquals(2, source.getQueries().size());
        }

        @Test
        void xlsFileProcessedCorrectly() throws IOException {
            File file = createXlsFile("data.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("products");
                row.createCell(1).setCellValue("Widget");
                row.createCell(2).setCellValue("9.99");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertFalse(source.getQueries().isEmpty());
            assertEquals(1, source.getQueries().size());
        }
    }

    @Nested
    class CellTypes {
        @Test
        void numericCellConverted() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("data");
                row.createCell(1).setCellValue(42.0);
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertFalse(source.getQueries().isEmpty());
            assertTrue(source.getQueries().get(0).contains("42.0"));
        }

        @Test
        void stringCellUsedAsIs() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("data");
                row.createCell(1).setCellValue("hello");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertTrue(source.getQueries().get(0).contains("hello"));
        }

        @Test
        void booleanCellConverted() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("flags");
                row.createCell(1).setCellValue(true);
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertTrue(source.getQueries().get(0).contains("true"));
        }

        @Test
        void unsupportedCellTypeThrows() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("data");
                Cell cell = row.createCell(1);
                cell.setCellFormula("1+1");
            });
            assertThrows(IllegalArgumentException.class,
                    () -> ExcelDatasetParser.getSource(file));
        }

        @Test
        void nullCellReturnsEmpty() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("data");
                row.createCell(1).setCellValue("val1");
                // skip cell 2 (null)
                row.createCell(3).setCellValue("val3");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertFalse(source.getQueries().isEmpty());
        }

        @Test
        void multipleValuesJoinedWithComma() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("users");
                row.createCell(1).setCellValue("John");
                row.createCell(2).setCellValue("Doe");
                row.createCell(3).setCellValue("30");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            String query = source.getQueries().get(0);
            assertTrue(query.contains("John"));
            assertTrue(query.contains("Doe"));
            assertTrue(query.contains("30"));
        }
    }

    @Nested
    class ErrorHandling {
        @Test
        void missingTableNameThrows() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                // no cell at index 0
                row.createCell(1).setCellValue("value");
            });
            assertThrows(IllegalArgumentException.class,
                    () -> ExcelDatasetParser.getSource(file));
        }

        @Test
        void nonExistentFileThrowsRuntimeException() {
            File file = new File(tempDir, "nonexistent.xls");
            assertThrows(RuntimeException.class,
                    () -> ExcelDatasetParser.getSource(file));
        }

        @Test
        void corruptXlsFileThrowsRuntimeException() throws IOException {
            File file = new File(tempDir, "corrupt.xls");
            java.nio.file.Files.writeString(file.toPath(), "not an excel file");
            assertThrows(RuntimeException.class,
                    () -> ExcelDatasetParser.getSource(file));
        }
    }

    @Nested
    class SqlFormat {
        @Test
        void generatedQueryContainsInsertInto() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("users");
                row.createCell(1).setCellValue("John");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertTrue(source.getQueries().get(0).startsWith("INSERT INTO"));
        }

        @Test
        void generatedQueryContainsTableName() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("my_table");
                row.createCell(1).setCellValue("value");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            assertTrue(source.getQueries().get(0).contains("my_table"));
        }
    }

    @Nested
    class AdditionalCellBehavior {
        @Test
        void singleValueNoTrailingComma() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("users");
                row.createCell(1).setCellValue("only_value");
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            String query = source.getQueries().get(0);
            assertTrue(query.contains("only_value"));
            assertFalse(query.endsWith(","));
        }

        @Test
        void mixedCellTypes() throws IOException {
            File file = createXlsFile("test.xls", wb -> {
                Sheet sheet = wb.createSheet();
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("mixed");
                row.createCell(1).setCellValue("text");
                row.createCell(2).setCellValue(42.0);
                row.createCell(3).setCellValue(true);
            });
            ListSource source = ExcelDatasetParser.getSource(file);
            String query = source.getQueries().get(0);
            assertTrue(query.contains("text"));
            assertTrue(query.contains("42.0"));
            assertTrue(query.contains("true"));
        }

        @Test
        void emptySheetProducesNoQueries() throws IOException {
            File file = createXlsFile("test.xls", wb -> wb.createSheet());
            ListSource source = ExcelDatasetParser.getSource(file);
            assertTrue(source.getQueries().isEmpty());
        }
    }

    private File createXlsFile(final String name, final WorkbookPopulator populator) throws IOException {
        File file = new File(tempDir, name);
        try (Workbook wb = new HSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            populator.populate(wb);
            wb.write(fos);
        }
        return file;
    }

    @FunctionalInterface
    interface WorkbookPopulator {
        void populate(Workbook wb);
    }
}
