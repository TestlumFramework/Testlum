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
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;

@Slf4j
@InterpreterForClass(CsvCommands.class)
public class CsvInterpreter extends AbstractInterpreter<CsvCommands> {

    private static final String SQL_INSERT = "INSERT INTO %s VALUES (%s);";

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    public CsvInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final CsvCommands csvCommands, final CommandResult result) {
        List<Source> sources = csvCommands.getCsvFile().stream()
                .map(this::getSource)
                .collect(Collectors.toList());
        applyPatches(csvCommands, result, sources);
    }

    private Source getSource(final String csvFile) {
        File csv = getCsvFileByPath(csvFile);
        List<String> commands = readAllLines(csv);
        return getSource(commands);
    }

    private File getCsvFileByPath(final String pathToFile) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        File csvFolder = TestResourceSettings.getInstance().getCsvFolder();
        return fileSearcher.search(csvFolder, pathToFile);
    }

    private List<String> readAllLines(final File csvFile) {
        try {
            return Files.readAllLines(csvFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Source getSource(final List<String> commands) {
        List<String> queries = commands.stream()
                .map(command ->
                        String.format(SQL_INSERT, getTableName(command), getValues(command)))
                .collect(Collectors.toList());
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

    private void applyPatches(final CsvCommands csvCommands, final CommandResult result,
                              final List<Source> sources) {
        log.info(ALIAS_LOG, csvCommands.getAlias());
        result.put("patches", new ArrayList<>(sources));
        postgresSqlOperation.apply(sources, csvCommands.getAlias());
    }
}
