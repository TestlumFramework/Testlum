package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.source.FileSource;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.migration.CsvMigration;
import com.knubisoft.cott.testing.framework.interpreter.lib.migration.ExcelMigration;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Migrate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.CSV_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLSX_EXTENSION;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.XLS_EXTENSION;
import static com.knubisoft.cott.testing.framework.util.LogMessage.ALIAS_LOG;
import static com.knubisoft.cott.testing.framework.util.LogMessage.ERROR_DURING_DB_MIGRATION_LOG;
import static com.knubisoft.cott.testing.framework.util.LogMessage.NAME_FOR_MIGRATION_MUST_PRESENT;
import static com.knubisoft.cott.testing.framework.util.LogMessage.PATCH_PATH_LOG;

@Slf4j
@InterpreterForClass(Migrate.class)
public class MigrateInterpreter extends AbstractInterpreter<Migrate> {

    @Autowired(required = false)
    private NameToAdapterAlias nameToAdapterAlias;

    public MigrateInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Migrate migrate, final CommandResult result) {
        String storageName = migrate.getName().name();
        String alias = migrate.getAlias();
        List<String> patches = migrate.getDataset();
        ResultUtil.addMigrateMetaData(storageName, alias, patches, result);
        if (StringUtils.isBlank(storageName)) {
            throw new DefaultFrameworkException(NAME_FOR_MIGRATION_MUST_PRESENT);
        }
        log.info(ALIAS_LOG, alias);
        patches.forEach(patch -> migrate(FileSearcher.searchFileFromDataFolder(patch), storageName, alias));
    }

    private void migrate(final File patch,
                         final String storageName,
                         final String databaseName) {
        try {
            log.info(PATCH_PATH_LOG, patch.getAbsolutePath());
            Source source = getSpecificSource(patch);
            applyPatches(source, storageName, databaseName);
        } catch (Exception e) {
            log.error(ERROR_DURING_DB_MIGRATION_LOG, e);
            throw new DefaultFrameworkException(e);
        }
    }

    private Source getSpecificSource(final File patch) {
        String fileName = patch.getName();
        if (fileName.endsWith(XLSX_EXTENSION) || fileName.endsWith(XLS_EXTENSION)) {
            return ExcelMigration.getSource(patch);
        } else if (fileName.endsWith(CSV_EXTENSION)) {
            return CsvMigration.getSource(patch);
        }
        return getSource(patch);
    }


    private void applyPatches(final Source patches,
                              final String storageName,
                              final String databaseName) {
        if (patches != null) {
            NameToAdapterAlias.Metadata metadata = nameToAdapterAlias
                    .getByNameOrThrow(storageName + UNDERSCORE + databaseName);
            metadata.getStorageOperation().apply(patches, databaseName);
        }
    }

    private FileSource getSource(final File patch) {
        return new FileSource(patch);
    }
}
