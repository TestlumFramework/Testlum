package com.knubisoft.cott.testing.framework.configuration.sendgrid;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.cott.testing.model.AliasEnv;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Sendgrid;
import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSendgridEnabledCondition.class})
public class SendgridConfiguration {

    @Bean
    public Map<AliasEnv, SendGrid> sendGrid() {
        Map<AliasEnv, SendGrid> sendGridMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addToMap(integrations, env, sendGridMap));
        return sendGridMap;
    }

    public void addToMap(final Integrations integrations,
                         final String env,
                         final Map<AliasEnv, SendGrid> sendGridMap) {
        for (Sendgrid sendgrid : integrations.getSendgridIntegration().getSendgrid()) {
            if (sendgrid.isEnabled()) {
                sendGridMap.put(new AliasEnv(sendgrid.getAlias(), env), new SendGrid(sendgrid.getApiKey()));
            }
        }
    }
}
