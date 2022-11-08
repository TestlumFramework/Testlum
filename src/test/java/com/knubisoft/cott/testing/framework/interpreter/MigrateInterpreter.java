package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.source.FileSource;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.scenario.Migrate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NAME_FOR_MIGRATION_MUST_PRESENT;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.ALIAS_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.DATASET_PATH_LOG;

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
        List<String> datasets = migrate.getDataset();
        ResultUtil.addMigrateMetaData(storageName, alias, datasets, result);
        if (StringUtils.isBlank(storageName)) {
            throw new DefaultFrameworkException(NAME_FOR_MIGRATION_MUST_PRESENT);
        }
        log.info(ALIAS_LOG, alias);
        migrate(datasets, storageName, alias);
    }

    private void migrate(final List<String> datasets,
                         final String storageName,
                         final String databaseName) {
        try {
            List<Source> sourceList = createSourceList(datasets);
            applyDatasets(sourceList, storageName, databaseName);
        } catch (Exception e) {
            LogUtil.logException(e);
            throw new DefaultFrameworkException(e);
        }
    }

    private List<Source> createSourceList(final List<String> datasets) {
        return datasets.stream()
                .map(this::createSource)
                .collect(Collectors.toList());
    }

    private FileSource createSource(final String datasetName) {
        File dataset = FileSearcher.searchFileFromDataFolder(datasetName);
        log.info(DATASET_PATH_LOG, dataset.getAbsolutePath());
        return new FileSource(dataset);
    }

    private void applyDatasets(final List<Source> datasets,
                              final String storageName,
                              final String databaseName) {
        if (!datasets.isEmpty()) {
            NameToAdapterAlias.Metadata metadata = nameToAdapterAlias
                    .getByNameOrThrow(storageName + UNDERSCORE + databaseName);
            metadata.getStorageOperation().apply(datasets, databaseName);
        }
    }
}
