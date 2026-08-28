package com.testlum.testing.framework.parser;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public final class CSVParser {

    public static final int HEADER_ROW = 0;

    private static final String COLUMN_JOINER = ", ";

    private final FileSearcher fileSearcher;

    public List<Map<String, String>> parseVariations(final String fileName) {
        List<String[]> variations = readVariationsFile(fileName);
        return getVariationsMapList(fileName, variations);
    }

    private List<String[]> readVariationsFile(final String variationFileName) {
        File file = fileSearcher.searchFileFromDataFolder(variationFileName);
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             CSVReader reader = new CSVReader(inputStreamReader)) {
            return reader.readAll();
        } catch (IOException | CsvException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private List<Map<String, String>> getVariationsMapList(final String fileName, final List<String[]> variations) {
        List<Map<String, String>> mapList = new ArrayList<>(variations.size());
        if (variations.isEmpty()) {
            return mapList;
        }
        String[] headerRow = variations.get(HEADER_ROW);
        for (int rowNumber = HEADER_ROW + 1; rowNumber < variations.size(); rowNumber++) {
            addRow(fileName, headerRow, variations.get(rowNumber), rowNumber, mapList);
        }
        return mapList;
    }

    private void addRow(final String fileName,
                        final String[] headerRow,
                        final String[] dataRow,
                        final int rowNumber,
                        final List<Map<String, String>> mapList) {
        if (isEmptyLine(dataRow)) {
            return;
        }
        validateRowSize(fileName, headerRow, dataRow, rowNumber);
        mapList.add(getRowMap(headerRow, dataRow));
    }

    private boolean isEmptyLine(final String[] dataRow) {
        return dataRow.length == 0 || dataRow.length == 1 && StringUtils.isBlank(dataRow[HEADER_ROW]);
    }

    private void validateRowSize(final String fileName,
                                 final String[] headerRow,
                                 final String[] dataRow,
                                 final int rowNumber) {
        if (headerRow.length != dataRow.length) {
            throw new DefaultFrameworkException(ExceptionMessage.VARIATION_ROW_COLUMNS_MISMATCH,
                    fileName, headerRow.length, String.join(COLUMN_JOINER, headerRow),
                    rowNumber + 1, dataRow.length);
        }
    }

    private Map<String, String> getRowMap(final String[] headerRow,
                                          final String[] dataRow) {
        Map<String, String> rowMap = new HashMap<>(headerRow.length);
        for (int columnNumber = 0; columnNumber < headerRow.length; columnNumber++) {
            rowMap.put(headerRow[columnNumber], dataRow[columnNumber]);
        }
        return rowMap;
    }
}
