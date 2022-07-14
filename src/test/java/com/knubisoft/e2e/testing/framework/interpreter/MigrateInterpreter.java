package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.db.source.FileSource;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.ResultUtil;
import com.knubisoft.e2e.testing.model.scenario.Migrate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ALIAS_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ERROR_DURING_DB_MIGRATION_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAME_FOR_MIGRATION_MUST_PRESENT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.PATCH_PATH_LOG;

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
        List<String> patches = migrate.getPatches();
        ResultUtil.addMigrateMetaData(storageName, alias, patches, result);
        if (StringUtils.isBlank(storageName)) {
            throw new DefaultFrameworkException(NAME_FOR_MIGRATION_MUST_PRESENT);
        }
        log.info(ALIAS_LOG, alias);
        migrate(patches, storageName, alias);
    }

    private void migrate(final List<String> patches,
                         final String storageName,
                         final String databaseName) {
        try {
            List<Source> sourceList = createSourceList(patches);
            applyPatches(sourceList, storageName, databaseName);
        } catch (Exception e) {
            log.error(ERROR_DURING_DB_MIGRATION_LOG, e);
            throw new DefaultFrameworkException(e);
        }
    }

    private List<Source> createSourceList(final List<String> patches) {
        File patchesFolder = TestResourceSettings.getInstance().getPatchesFolder();
        return patches.stream()
                .map(each -> createFileSource(patchesFolder, each))
                .collect(Collectors.toList());
    }

    private FileSource createFileSource(final File patchesFolder,
                                        final String patchFileName) {
        File patch = new File(patchesFolder, patchFileName);
        log.info(PATCH_PATH_LOG, patch.getAbsolutePath());
        return new FileSource(patch);
    }

    private void applyPatches(final List<Source> patches,
                              final String storageName,
                              final String databaseName) {
        if (!patches.isEmpty()) {
            NameToAdapterAlias.Metadata metadata = nameToAdapterAlias
                    .getByNameOrThrow(storageName + UNDERSCORE + databaseName);
            metadata.getStorageOperation().apply(patches, databaseName);
        }
    }
}
