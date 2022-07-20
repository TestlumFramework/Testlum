package com.knubisoft.e2e.testing.framework.interpreter.lib.migrate;

import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.db.source.ListSource;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.framework.util.ResultUtil;
import com.knubisoft.e2e.testing.model.scenario.Migrate;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;

@Slf4j
public class CsvMigration {

    private static final String SQL_INSERT = "INSERT INTO %s VALUES (%s);";

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    protected void acceptImpl(final Migrate csvCommands, final CommandResult result) {
        String alias = csvCommands.getAlias();
        List<String> csvFiles = csvCommands.getData();
        ResultUtil.addMigrateMetaData(ResultUtil.POSTGRES, alias, csvFiles, result);
        List<Source> sources = csvFiles.stream()
                .map(this::getSource)
                .collect(Collectors.toList());
        log.info(ALIAS_LOG, alias);
        postgresSqlOperation.apply(sources, alias);
    }

    private Source getSource(final String csvFile) {
        File csv = FileSearcher.searchFileFromDataFolder(csvFile);
        List<String> commands = readAllLines(csv);
        return prepareSource(commands);
    }

    private List<String> readAllLines(final File csvFile) {
        try {
            return Files.readAllLines(csvFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Source prepareSource(final List<String> commands) {
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
}
