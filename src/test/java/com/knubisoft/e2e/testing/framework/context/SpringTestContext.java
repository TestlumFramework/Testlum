package com.knubisoft.e2e.testing.framework.context;

import com.knubisoft.e2e.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.e2e.testing.framework.report.ReportGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedCaseInsensitiveMap;

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
    public GlobalScenarioStatCollector createNewStatCollector() {
        return new GlobalScenarioStatCollector();
    }

    @Bean
    public ReportGenerator createReportConfiguration() {
        return new ReportGenerator();
    }
}
