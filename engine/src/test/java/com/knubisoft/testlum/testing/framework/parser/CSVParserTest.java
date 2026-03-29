package com.knubisoft.testlum.testing.framework.parser;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CSVParserTest {

    @Mock
    private FileSearcher fileSearcher;

    @InjectMocks
    private CSVParser csvParser;

    @TempDir
    private Path tempDir;

    @Test
    void headerRowConstantIsZero() {
        assertEquals(0, CSVParser.HEADER_ROW);
    }

    @Test
    void parseVariationsWithHeaderAndOneDataRow() throws IOException {
        File csvFile = createCsvFile("test.csv", "name,age,city\nAlice,30,Kyiv");
        when(fileSearcher.searchFileFromDataFolder("test.csv")).thenReturn(csvFile);

        List<Map<String, String>> result = csvParser.parseVariations("test.csv");

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).get("name"));
        assertEquals("30", result.get(0).get("age"));
        assertEquals("Kyiv", result.get(0).get("city"));
    }

    @Test
    void parseVariationsWithHeaderAndMultipleDataRows() throws IOException {
        File csvFile = createCsvFile("multi.csv",
                "name,age\nAlice,30\nBob,25\nCharlie,35");
        when(fileSearcher.searchFileFromDataFolder("multi.csv")).thenReturn(csvFile);

        List<Map<String, String>> result = csvParser.parseVariations("multi.csv");

        assertEquals(3, result.size());
        assertEquals("Alice", result.get(0).get("name"));
        assertEquals("Bob", result.get(1).get("name"));
        assertEquals("Charlie", result.get(2).get("name"));
        assertEquals("30", result.get(0).get("age"));
        assertEquals("25", result.get(1).get("age"));
        assertEquals("35", result.get(2).get("age"));
    }

    @Test
    void parseVariationsWithHeaderOnlyReturnsEmptyList() throws IOException {
        File csvFile = createCsvFile("header-only.csv", "name,age,city");
        when(fileSearcher.searchFileFromDataFolder("header-only.csv")).thenReturn(csvFile);

        List<Map<String, String>> result = csvParser.parseVariations("header-only.csv");

        assertTrue(result.isEmpty());
    }

    private File createCsvFile(final String fileName, final String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, content);
        return filePath.toFile();
    }
}
