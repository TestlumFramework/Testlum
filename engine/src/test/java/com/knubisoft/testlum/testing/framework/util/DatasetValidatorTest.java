package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetValidatorTest {

    @Mock
    private FileSearcher fileSearcher;

    @InjectMocks
    private DatasetValidator datasetValidator;

    @Test
    void postgresWithSqlFileDoesNotThrow() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.sql"));
        assertDoesNotThrow(
                () -> datasetValidator.validateDatasetByExtension("dataset.sql", StorageName.POSTGRES));
    }

    @Test
    void postgresWithCsvFileDoesNotThrow() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.csv"));
        assertDoesNotThrow(
                () -> datasetValidator.validateDatasetByExtension("dataset.csv", StorageName.POSTGRES));
    }

    @Test
    void postgresWithXlsxFileDoesNotThrow() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.xlsx"));
        assertDoesNotThrow(
                () -> datasetValidator.validateDatasetByExtension("dataset.xlsx", StorageName.POSTGRES));
    }

    @Test
    void postgresWithJsonFileThrowsException() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.json"));
        assertThrows(DefaultFrameworkException.class,
                () -> datasetValidator.validateDatasetByExtension("dataset.json", StorageName.POSTGRES));
    }

    @Test
    void redisWithTxtFileDoesNotThrow() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.txt"));
        assertDoesNotThrow(
                () -> datasetValidator.validateDatasetByExtension("dataset.txt", StorageName.REDIS));
    }

    @Test
    void redisWithSqlFileThrowsException() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.sql"));
        assertThrows(DefaultFrameworkException.class,
                () -> datasetValidator.validateDatasetByExtension("dataset.sql", StorageName.REDIS));
    }

    @Test
    void mongodbWithJsonFileDoesNotThrow() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.json"));
        assertDoesNotThrow(
                () -> datasetValidator.validateDatasetByExtension("dataset.json", StorageName.MONGODB));
    }

    @Test
    void mongodbWithBsonFileDoesNotThrow() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.bson"));
        assertDoesNotThrow(
                () -> datasetValidator.validateDatasetByExtension("dataset.bson", StorageName.MONGODB));
    }

    @Test
    void mongodbWithTxtFileThrowsException() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.txt"));
        assertThrows(DefaultFrameworkException.class,
                () -> datasetValidator.validateDatasetByExtension("dataset.txt", StorageName.MONGODB));
    }

    @Test
    void dynamoWithPartiqlFileDoesNotThrow() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.partiql"));
        assertDoesNotThrow(
                () -> datasetValidator.validateDatasetByExtension("dataset.partiql", StorageName.DYNAMO));
    }

    @Test
    void dynamoWithCsvFileThrowsException() {
        when(fileSearcher.searchFileFromDataFolder(anyString())).thenReturn(new File("dataset.csv"));
        assertThrows(DefaultFrameworkException.class,
                () -> datasetValidator.validateDatasetByExtension("dataset.csv", StorageName.DYNAMO));
    }
}
