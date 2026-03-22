package com.knubisoft.testlum.testing.framework.db.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.MigrationConstant;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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
                .map(command -> String.format(MigrationConstant.SQL_INSERT, getTableName(command), getValues(command)))
                .toList();
        return new ListSource(queries);
    }

    private String getTableName(final String command) {
        String table = command.split(DelimiterConstant.COMMA)[0];
        if (StringUtils.isBlank(table)) {
            throw new IllegalArgumentException("Please, put in the first column your table name");
        }
        return table;
    }

    private String getValues(final String values) {
        String subColumns = values.substring(values.indexOf(DelimiterConstant.COMMA) + 1);
        if (StringUtils.isBlank(subColumns)) {
            throw new IllegalArgumentException("Please, fill some values to create a SQL statement");
        }
        return subColumns;
    }
}

