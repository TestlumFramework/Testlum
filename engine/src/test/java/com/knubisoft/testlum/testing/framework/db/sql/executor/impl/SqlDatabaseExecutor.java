package com.knubisoft.testlum.testing.framework.db.sql.executor.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SqlDatabaseExecutor extends AbstractSqlExecutor {

    private final AliasEnv aliasEnv;

    public SqlDatabaseExecutor(DataSource dataSource, AliasEnv aliasEnv) {
        super(dataSource);
        this.aliasEnv = aliasEnv;
    }

    public AliasEnv getAliasEnv() {
        return aliasEnv;
    }

    @Override
    public void truncate() {
        SqlDatabase database = ConfigProviderImpl.GlobalTestConfigurationProvider
                .getDefaultIntegrations()
                .getSqlDatabaseIntegration()
                .getSqlDatabase()
                .stream()
                .filter(db -> db.getAlias().equals(aliasEnv.getAlias()))
                .findFirst()
                .orElse(null);

        if (database != null && database.getCustomTruncate() != null) {
            String fileName = database.getCustomTruncate().getTruncateFile();
            if (fileName != null && !fileName.isEmpty()) {
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

                    return;
                } catch (Exception e) {
                    log.error("Failed to execute truncate file '{}': {}", fileName, e.getMessage(), e);
                }
            }
        }

        log.info("No custom truncate file defined for alias '{}'. Fallback logic can be placed here.", aliasEnv.getAlias());
    }

    private List<String> splitSqlStatements(String sql) {
        return Arrays.stream(sql.split(";\\s*(\\r?\\n)?"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}