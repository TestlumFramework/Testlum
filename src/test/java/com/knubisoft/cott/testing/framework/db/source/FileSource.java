package com.knubisoft.cott.testing.framework.db.source;

import com.knubisoft.cott.testing.framework.util.CsvDatasetParser;
import com.knubisoft.cott.testing.framework.util.ExcelDatasetParser;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.CSV_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLSX_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLS_EXTENSION;

public class FileSource implements Source {

    private final ListSource listSource;

    @SneakyThrows
    public FileSource(final File file) {
        final String datasetName = file.getName();
        if (file.getName().endsWith(XLSX_EXTENSION) || datasetName.endsWith(XLS_EXTENSION)) {
            this.listSource = ExcelDatasetParser.getSource(file);
        } else if (datasetName.endsWith(CSV_EXTENSION)) {
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
