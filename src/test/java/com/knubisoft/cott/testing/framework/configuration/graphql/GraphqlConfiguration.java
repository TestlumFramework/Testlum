package com.knubisoft.cott.testing.framework.configuration.graphql;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnGraphqlEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Graphql;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(OnGraphqlEnabledCondition.class)
public class GraphqlConfiguration {
    @Bean
    public Map<String, String> apiUrlsMap() {
        final Map<String, String> apiUrls = new HashMap<>();
        for (Graphql graphql : GlobalTestConfigurationProvider.getIntegrations().getGraphqlIntegration().getGraphql()) {
            if (graphql.isEnabled()) {
                apiUrls.put(graphql.getApiAlias(), graphql.getUrl());
            }
        }
        return apiUrls;
    }
}
