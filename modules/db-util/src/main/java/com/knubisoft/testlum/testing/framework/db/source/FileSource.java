package com.knubisoft.testlum.testing.framework.db.source;

import com.knubisoft.testlum.testing.framework.constant.MigrationConstant;
import com.knubisoft.testlum.testing.framework.db.util.CsvDatasetParser;
import com.knubisoft.testlum.testing.framework.db.util.ExcelDatasetParser;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class FileSource implements Source {

    private final ListSource listSource;

    @SneakyThrows
    public FileSource(final File file) {
        final String datasetName = file.getName();
        if (file.getName().endsWith(MigrationConstant.XLSX_EXTENSION)
                || datasetName.endsWith(MigrationConstant.XLS_EXTENSION)) {
            this.listSource = ExcelDatasetParser.getSource(file);
        } else if (datasetName.endsWith(MigrationConstant.CSV_EXTENSION)) {
            this.listSource = CsvDatasetParser.getSource(file);
        } else {
            String queries = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            this.listSource = new ListSource(Arrays.asList(queries.split(QUERY_DELIMITER)));
        }
    }

    @Override
    public List<String> getQueries() {
        return listSource.getQueries();
    }
}
