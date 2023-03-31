package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.db.source.ListSource;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.SQL_INSERT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@UtilityClass
public class CsvDatasetParser {

    public ListSource getSource(final File csvFile) {
        List<String> commands = readAllLines(csvFile);
        return prepareSource(commands);
    }

    private List<String> readAllLines(final File csvFile) {
        try {
            return Files.readAllLines(csvFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ListSource prepareSource(final List<String> commands) {
        List<String> queries = commands.stream()
                .map(command -> String.format(SQL_INSERT, getTableName(command), getValues(command)))
                .collect(Collectors.toList());
        return new ListSource(queries);
    }

    private String getTableName(final String command) {
        String table = command.split(DelimiterConstant.COMMA)[0];
        if (isBlank(table)) {
            throw new IllegalArgumentException("Please, put in the first column your table name");
        }
        return table;
    }

    private String getValues(final String values) {
        String subColumns = values.substring(values.indexOf(DelimiterConstant.COMMA) + 1);
        if (isBlank(subColumns)) {
            throw new IllegalArgumentException("Please, fill some values to create a SQL statement");
        }
        return subColumns;
    }
}

