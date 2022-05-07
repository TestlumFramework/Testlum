package com.knubisoft.e2e.testing.framework.interpreter.formater;

import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CsvFormatter {

    private static final String SQL_INSERT = "INSERT INTO %s %s";
    private static final String SQL_VALUES = "VALUES (%s);";

    public List<String> convertFileToString(final File csvFile) {
        try {
            return Files.readAllLines(csvFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Source getSource(final List<String> commands) {
        List<String> queries = commands.stream().map(command -> {
            String tableName = getTableName(command);
            String values = getValues(command);
            if (tableName.isEmpty() || values.isEmpty()) {
                throw new IllegalArgumentException("The syntax of query isn't correct.");
            }
            return String.format(SQL_INSERT, tableName, values);
        }).collect(Collectors.toList());
        return new ListSource(queries);
    }

    private String getTableName(final String command) {
        return command.split(DelimiterConstant.HASH)[0] != null
                ? command.split(DelimiterConstant.HASH)[0]
                : DelimiterConstant.EMPTY;
    }

    private String getValues(final String values) {
        String line = values.split(DelimiterConstant.HASH)[1] != null
                ? values.split(DelimiterConstant.HASH)[1]
                : DelimiterConstant.EMPTY;
        if (line.isEmpty()) {
            return DelimiterConstant.EMPTY;
        }
        return String.format(SQL_VALUES, String.join(DelimiterConstant.EMPTY, line));
    }
}
