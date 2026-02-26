package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.report.ReportGenerator;
import com.knubisoft.testlum.testing.framework.report.ReportGeneratorFactory;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class SpringTestContext {

    private final GlobalTestConfiguration globalTestConfiguration;
    private final ReportGeneratorFactory reportGeneratorFactory;
    private final List<Environment> environments;

    @Bean
    public AliasToStorageOperation getAliasToStorageOperation(final List<AliasAdapter> aliasAdapters) {
        Map<String, AbstractStorageOperation> aliasMap = new LinkedCaseInsensitiveMap<>(aliasAdapters.size());
        for (AliasAdapter aliasAdapter : aliasAdapters) {
            aliasAdapter.apply(aliasMap);
        }
        return new AliasToStorageOperationImpl(aliasMap);
    }

    @Bean
    public EnvManager envManager() {
        boolean isParallelExecutionEnabled = globalTestConfiguration.isParallelExecution();
        final List<Environment> envList = isParallelExecutionEnabled
                ? environments : Collections.singletonList(environments.get(0));
        return new EnvManager(envList);
    }

    @Primary
    @Bean
    public ReportGenerator reportGenerator() {
        return reportGeneratorFactory.create(globalTestConfiguration.getReport());
    }
}
