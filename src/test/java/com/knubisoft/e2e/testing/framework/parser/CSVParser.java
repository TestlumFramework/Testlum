package com.knubisoft.e2e.testing.framework.parser;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public final class CSVParser {

    public static final int HEADER_ROW = 0;

    @SneakyThrows
    public List<Map<String, String>> parseVariations(final String fileName) {
        List<String[]> variations = readVariationsFile(fileName);
        return getVariationsMapList(variations);
    }

    //CHECKSTYLE:OFF
    @SneakyThrows
    private List<String[]> readVariationsFile(final String variationFileName) {
        TestResourceSettings settings = TestResourceSettings.getInstance();
        File file = new FileSearcher(
                settings.getTestResourcesFolder(),
               null).fileByNameAndExtension(variationFileName);
        FileInputStream fileInputStream = new FileInputStream(file);

        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        List<String[]> variations;
        try (CSVReader reader = new CSVReader(inputStreamReader)) {
            variations = reader.readAll();
        }
        return variations;
    }
    //CHECKSTYLE:ON

    private List<Map<String, String>> getVariationsMapList(final List<String[]> variations) {
        List<Map<String, String>> mapList = new ArrayList<>(variations.size());
        for (int rowNumber = 1; rowNumber < variations.size(); rowNumber++) {
            enrichMapList(mapList, variations.get(HEADER_ROW), variations.get(rowNumber));
        }
        return mapList;
    }

    private void enrichMapList(final List<Map<String, String>> mapList,
                               final String[] headerRow,
                               final String[] dataRow) {
        Map<String, String> rowMap = new HashMap<>(headerRow.length);
        for (int columnNumber = 0; columnNumber < headerRow.length; columnNumber++) {
            rowMap.put(headerRow[columnNumber], dataRow[columnNumber]);
        }
        mapList.add(rowMap);
    }
}
