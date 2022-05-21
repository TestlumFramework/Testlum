package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.model.scenario.CsvCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(CsvCommands.class)
public class CsvInterpreter extends AbstractInterpreter<CsvCommands> {

    private static final String SQL_INSERT = "INSERT INTO %s %s";
    private static final String SQL_VALUES = "VALUES (%s);";

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public CsvInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final CsvCommands csvCommands, final CommandResult result) {
        csvCommands.getCsvFile().forEach(csvFile -> {
            File csv = getCsvFileByPath(csvFile);
            List<String> commands = convertFileToString(csv);
            Source source = getSource(commands);
            postgresSqlOperation.apply(source, csvCommands.getAlias());
        });
    }

    private File getCsvFileByPath(final String pathToFile) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        File csvFolder = TestResourceSettings.getInstance().getCsvFolder();
        return fileSearcher.search(csvFolder, pathToFile);
    }

    private List<String> convertFileToString(final File csvFile) {
        try {
            return Files.readAllLines(csvFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Source getSource(final List<String> commands) {
        List<String> queries = commands.stream().map(command -> {
            String tableName = getTableName(command);
            String values = getValues(command);
            if (!tableName.isEmpty() && !values.isEmpty()) {
                return String.format(SQL_INSERT, tableName, values);
            }
            throw new IllegalArgumentException("The syntax of query isn't correct.");
        }).collect(Collectors.toList());
        return new ListSource(queries);
    }

    private String getTableName(final String command) {
        return command.split(DelimiterConstant.COMA)[0] != null && !command.isEmpty()
                ? command.split(DelimiterConstant.COMA)[0]
                : DelimiterConstant.EMPTY;
    }

    private String getValues(final String values) {
        StringBuilder builder = new StringBuilder();
        String[] splitValues = values.split(DelimiterConstant.COMA);
        for (int i = 1; i < splitValues.length; i++) {
            if (i == splitValues.length - 1) {
                builder.append(splitValues[i]);
            } else {
                builder.append(splitValues[i]).append(DelimiterConstant.COMA);
            }
        }
        return String.format(SQL_VALUES, builder.toString());
    }
}
