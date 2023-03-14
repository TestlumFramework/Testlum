package com.knubisoft.cott.testing.framework.context;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.report.ReportGenerator;
import com.knubisoft.cott.testing.framework.report.ReportGeneratorFactory;
import com.knubisoft.cott.testing.model.global_config.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return new EnvManager(GlobalTestConfigurationProvider.getEnabledEnvironments().stream()
                .map(Environment::getFolder)
                .collect(Collectors.toList()));
    }

    @Bean
    public ReportGenerator reportGenerator() {
        return ReportGeneratorFactory.create(GlobalTestConfigurationProvider.provide().getReport());
    }
}
