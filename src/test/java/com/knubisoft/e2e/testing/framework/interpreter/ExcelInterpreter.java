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
import com.knubisoft.e2e.testing.model.scenario.ExcelCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@InterpreterForClass(ExcelCommands.class)
public class ExcelInterpreter extends AbstractInterpreter<ExcelCommands> {

    private static final String SQL_INSERT = "INSERT INTO %s %s";
    private static final String SQL_VALUES = "VALUES (%s);";

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public ExcelInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final ExcelCommands excelCommands, final CommandResult result) {
        excelCommands.getExcelFile().forEach(excelFile -> {
            File excel = getCsvFileByPath(excelFile);
            List<String> commands = convertFileToString(excel);
            Source source = getSource(commands);
            postgresSqlOperation.apply(source, excelCommands.getAlias());
        });
    }

    private File getCsvFileByPath(final String pathToFile) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        File excelFolder = TestResourceSettings.getInstance().getExcelFolder();
        return fileSearcher.search(excelFolder, pathToFile);
    }

    private List<String> convertFileToString(final File excelFile) {
        try {
            return Files.readAllLines(excelFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Source getSource(final List<String> commands) {
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
        String tableName = command.substring(0, command.indexOf(DelimiterConstant.COMA));
        if (tableName.isEmpty()) {
            return DelimiterConstant.EMPTY;
        }
        return tableName;
    }

    private String getValues(final String values) {
        String line = values.substring(values.indexOf(DelimiterConstant.COMA) + 1);
        if (line.isEmpty()) {
            return DelimiterConstant.EMPTY;
        }
        return String.format(SQL_VALUES, String.join(DelimiterConstant.EMPTY, line));
    }
}
