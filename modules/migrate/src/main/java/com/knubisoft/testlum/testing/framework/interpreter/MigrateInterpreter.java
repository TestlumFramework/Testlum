package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasToStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.FileSource;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.db.util.LogUtil;
import com.knubisoft.testlum.testing.framework.db.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Migrate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@InterpreterForClass(Migrate.class)
public class MigrateInterpreter extends AbstractInterpreter<Migrate> {


    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String NAME_FOR_MIGRATION_MUST_PRESENT = "Data storage name for migration must present";
    private static final String DATASET_PATH_LOG = format(TABLE_FORMAT, "Migration dataset", "{}");

    @Autowired(required = false)
    private AliasToStorageOperation aliasToStorageOperation;

    public MigrateInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Migrate o, final CommandResult result) {
        Migrate migrate = injectCommand(o);
        String storageName = migrate.getName().name();
        String databaseAlias = migrate.getAlias();
        List<String> datasets = migrate.getDataset();
        if (StringUtils.isBlank(storageName)) {
            throw new DefaultFrameworkException(NAME_FOR_MIGRATION_MUST_PRESENT);
        }
        ResultUtil.addMigrateMetaData(storageName, databaseAlias, datasets, result);
        LogUtil.logAlias(databaseAlias);
        migrate(datasets, storageName, databaseAlias);
    }

    private void migrate(final List<String> datasets,
                         final String storageName,
                         final String databaseAlias) {
        List<Source> sourceList = createSourceList(datasets);
        applyDatasets(sourceList, storageName, databaseAlias);
    }

    private List<Source> createSourceList(final List<String> datasets) {
        return datasets.stream()
                .map(this::createSource)
                .map(this::injectQueries)
                .collect(Collectors.toList());
    }

    private Source createSource(final String datasetName) {
        File dataset = FileSearcher.searchFileFromDataFolder(datasetName);
        log.info(DATASET_PATH_LOG, dataset.getAbsolutePath());
        return new FileSource(dataset);
    }

    private Source injectQueries(final Source source) {
        List<String> queries = source.getQueries().stream()
                .map(this::inject)
                .collect(Collectors.toList());
        return new ListSource(queries);
    }

    private void applyDatasets(final List<Source> datasets,
                               final String storageName,
                               final String databaseAlias) {
        if (!datasets.isEmpty()) {
            String adapterName = storageName + DelimiterConstant.UNDERSCORE + databaseAlias;
            AbstractStorageOperation storageOperation = aliasToStorageOperation.getByNameOrThrow(adapterName);
            storageOperation.apply(datasets, databaseAlias);
        }
    }
}
