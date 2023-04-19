package com.knubisoft.testlum.testing.framework.context;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.report.ReportGenerator;
import com.knubisoft.testlum.testing.framework.report.ReportGeneratorFactory;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = {"com.knubisoft"})
public class SpringTestContext {

    @Bean
    public NameToAdapterAlias getNameToAdapterAlias(final List<AliasAdapter> aliasAdapters) {
        Map<String, NameToAdapterAlias.Metadata> aliasMap = new LinkedCaseInsensitiveMap<>(aliasAdapters.size());
        for (AliasAdapter aliasAdapter : aliasAdapters) {
            aliasAdapter.apply(aliasMap);
        }
        return new NameToAdapterAlias(aliasMap);
    }

    @Bean
    public EnvManager envManager() {
        boolean isParallelExecutionEnabled = GlobalTestConfigurationProvider.provide().isParallelExecution();
        List<Environment> enabledEnvList = GlobalTestConfigurationProvider.getEnabledEnvironments();
        final List<Environment> envList = isParallelExecutionEnabled
                ? enabledEnvList : Collections.singletonList(enabledEnvList.get(0));
        return new EnvManager(envList);
    }

    @Bean
    public ReportGenerator reportGenerator() {
        return ReportGeneratorFactory.create(GlobalTestConfigurationProvider.provide().getReport());
    }
}
