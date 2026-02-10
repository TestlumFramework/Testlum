package com.knubisoft.testlum.testing.framework.configuration.sendgrid;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnSendgridEnabledCondition.class})
@RequiredArgsConstructor
public class SendgridConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, SendGrid> sendGrid() {
        Map<AliasEnv, SendGrid> sendGridMap = new HashMap<>();
        GlobalTestConfigurationProvider.get().getIntegrations()
                .forEach((env, integrations) -> addToMap(integrations, env, sendGridMap));
        return sendGridMap;
    }

    public void addToMap(final Integrations integrations,
                         final String env,
                         final Map<AliasEnv, SendGrid> sendGridMap) {
        for (Sendgrid sendgrid : integrations.getSendgridIntegration().getSendgrid()) {
            if (sendgrid.isEnabled()) {
                SendGrid checkedSendGrid = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "SendGrid", sendgrid.getAlias()),
                        () -> new SendGrid(sendgrid.getApiKey()),
                        HealthCheckFactory.forSendGrid()
                );
                sendGridMap.put(new AliasEnv(sendgrid.getAlias(), env), checkedSendGrid);
            }
        }
    }
}
