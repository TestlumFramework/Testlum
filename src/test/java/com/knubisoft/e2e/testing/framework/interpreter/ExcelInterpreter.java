package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.model.scenario.ExcelCommands;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@InterpreterForClass(ExcelCommands.class)
public class ExcelInterpreter extends AbstractInterpreter<ExcelCommands> {

    private static final String SQL_INSERT = "INSERT INTO %s %s";
    private static final String SQL_VALUES = "VALUES (%s);";
    private static final String XLSX_EXTENSION = ".xlsx";
    private static final String EMPTY_CELL = "'";
    private static final String NULL_CELL = "NULL";

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public ExcelInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final ExcelCommands excelCommands, final CommandResult result) {
        excelCommands.getExcelFile().forEach(excelFile -> {
            applyExcelQueriesOrThrow(excelFile, excelCommands);
        });
    }

    private void applyExcelQueriesOrThrow(final String excelFile, final ExcelCommands excelCommands) {
        try {
            List<String> queries = getQueries(excelFile);
            postgresSqlOperation.apply(new ListSource(queries), excelCommands.getAlias());
        } catch (Exception ioe) {
            throw new RuntimeException("Can't create workbook class. Please, check "
                    + "if the extension of the file is correct (.xls or .xlsx)");
        }
    }

    private List<String> getQueries(final String excelFile) {
        Workbook workbook = getWorkbook(excelFile);
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            populateQueries(workbook.getSheetAt(i), queries);
        }
        return queries;
    }

    private Workbook getWorkbook(final String excelFile) {
        File file = getCsvFileByPath(excelFile);
        try {
            if (file.toString().endsWith(XLSX_EXTENSION)) {
                return new XSSFWorkbook(new FileInputStream(file));
            }
            return new HSSFWorkbook(new FileInputStream(file));
        } catch (IOException exception) {
            throw new RuntimeException("Can't find the file or can't signature of the excel "
                    + "file isn't correct. Please, create your excel file from an application "
                    + "or from google.");
        }
    }

    private File getCsvFileByPath(final String pathToFile) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        File excelFolder = TestResourceSettings.getInstance().getExcelFolder();
        return fileSearcher.search(excelFolder, pathToFile);
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
                builder.append(cellValue).append(DelimiterConstant.COMA);
            }
        }
        return String.format(SQL_VALUES, builder.toString());
    }

    private String getTableName(final Row row) {
        if (row.getCell(0) != null) {
            return getCellValue(row.getCell(0))
                    .replaceAll(DelimiterConstant.APOSTROPHE, DelimiterConstant.EMPTY);
        }
        return DelimiterConstant.EMPTY;
    }

    private String getCellValue(final Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case STRING:
                return processStringValue(cell);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                throw new IllegalArgumentException("A cell doesn't have any corresponds cell type");
        }
    }

    private String processStringValue(final Cell cell) {
        String cellValue = cell.getStringCellValue();
        if (cellValue.equals(EMPTY_CELL)) {
            return DelimiterConstant.APOSTROPHE + DelimiterConstant.APOSTROPHE;
        } else if (cellValue.equals(NULL_CELL)) {
            return cell.getStringCellValue()
                    .replaceFirst(DelimiterConstant.APOSTROPHE, DelimiterConstant.EMPTY);
        } else if (cellValue.endsWith(DelimiterConstant.APOSTROPHE)) {
            return DelimiterConstant.APOSTROPHE + cell.getStringCellValue();
        }
        return DelimiterConstant.APOSTROPHE + cell.getStringCellValue() + DelimiterConstant.APOSTROPHE;
    }
}
