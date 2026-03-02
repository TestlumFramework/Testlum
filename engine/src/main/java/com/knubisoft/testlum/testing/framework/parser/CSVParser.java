package com.knubisoft.testlum.testing.framework.parser;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.opencsv.CSVReader;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public final class CSVParser {

    public static final int HEADER_ROW = 0;

    private final FileSearcher fileSearcher;

    public List<Map<String, String>> parseVariations(final String fileName) {
        List<String[]> variations = readVariationsFile(fileName);
        return getVariationsMapList(variations);
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

    private List<Map<String, String>> getVariationsMapList(final List<String[]> variations) {
        List<Map<String, String>> mapList = new ArrayList<>(variations.size());
        for (int rowNumber = 1; rowNumber < variations.size(); rowNumber++) {
            mapList.add(getRowMap(variations.get(HEADER_ROW), variations.get(rowNumber)));
        }
        return mapList;
    }

    private Map<String, String> getRowMap(final String[] headerRow,
                                          final String[] dataRow) {
        Map<String, String> rowMap = new LinkedHashMap<>(headerRow.length);
        for (int columnNumber = 0; columnNumber < headerRow.length; columnNumber++) {
            rowMap.put(headerRow[columnNumber], dataRow[columnNumber]);
        }
        return rowMap;
    }
}
