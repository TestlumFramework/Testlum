package com.knubisoft.testlum.testing.framework.db.sql.executor.impl;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class SqlDatabaseExecutor extends AbstractSqlExecutor {

    private final AliasEnv aliasEnv;

    public SqlDatabaseExecutor(final DataSource dataSource, final AliasEnv aliasEnv) {
        super(dataSource);
        this.aliasEnv = aliasEnv;
    }

    @Override
    public void truncate() {
        SqlDatabase database = getSqlDatabase();

        if (database != null && database.getCustomTruncate() != null) {
            String fileName = database.getCustomTruncate().getTruncateFile();
            if (fileName != null && !fileName.isEmpty()) {
                doTruncate(fileName);
            }
        } else {
            log.info("No custom truncate file defined for alias '{}'. "
                    + "Fallback logic can be placed here.", aliasEnv.getAlias());
        }
    }

    private @Nullable SqlDatabase getSqlDatabase() {
        return GlobalTestConfigurationProvider.get()
                .getDefaultIntegrations()
                .getSqlDatabaseIntegration()
                .getSqlDatabase().stream()
                .filter(db -> db.getAlias().equals(aliasEnv.getAlias()))
                .findFirst().orElse(null);
    }

    private void doTruncate(final String fileName) {
        try {
            File truncateFile = FileSearcher.searchFileFromDataFolder(fileName);
            String sql = FileUtils.readFileToString(truncateFile, StandardCharsets.UTF_8);
            List<String> queries = splitSqlStatements(sql);

            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    log.info("Executing truncate SQL for alias '{}': {}", aliasEnv.getAlias(), query);
                    template.execute(query);
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute truncate file '{}': {}", fileName, e.getMessage(), e);
        }
    }

    private List<String> splitSqlStatements(final String sql) {
        return Arrays.stream(sql.split(";\\s*(\\r?\\n)?"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}