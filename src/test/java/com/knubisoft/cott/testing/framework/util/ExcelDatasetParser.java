package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import lombok.experimental.UtilityClass;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.SQL_INSERT;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLSX_EXTENSION;

@UtilityClass
public class ExcelDatasetParser {

    public ListSource getSource(final File excelFile) {
        Workbook workbook = getWorkbook(excelFile);
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            populateQueries(workbook.getSheetAt(i), queries);
        }
        return new ListSource(queries);
    }

    private Workbook getWorkbook(final File excelFile) {
        try {
            if (excelFile.toString().endsWith(XLSX_EXTENSION)) {
                return new XSSFWorkbook(Files.newInputStream(excelFile.toPath()));
            }
            return new HSSFWorkbook(Files.newInputStream(excelFile.toPath()));
        } catch (IOException exception) {
            throw new RuntimeException("Can't find the file or can't signature of the excel "
                    + "file isn't correct. Please, create your excel file from an application "
                    + "or from google.");
        }
    }

    private void populateQueries(final Sheet sheet, final List<String> queries) {
        for (Row row : sheet) {
            String values = getValues(row);
            String tableName = getTableName(row);
            queries.add(String.format(SQL_INSERT, tableName, values));
        }
    }

    private String getValues(final Row row) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String cellValue = getCellValue(cell);
            if (i == row.getLastCellNum() - 1) {
                builder.append(cellValue);
            } else {
                builder.append(cellValue).append(DelimiterConstant.COMMA);
            }
        }
        return builder.toString();
    }

    private String getTableName(final Row row) {
        if (row.getCell(0) != null) {
            return getCellValue(row.getCell(0));
        }
        throw new IllegalArgumentException("Please, fill any exist table in the xls/x file");
    }

    private String getCellValue(final Cell cell) {
        if (Objects.isNull(cell)) {
            return DelimiterConstant.EMPTY;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                throw new IllegalArgumentException("A cell doesn't have any corresponds cell type");
        }
    }
}

