package com.knubisoft.testlum.testing.framework.configuration.sendgrid;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSendgridEnabledCondition.class})
@RequiredArgsConstructor
public class SendgridConfiguration {

    private final ConnectionTemplate connectionTemplate;

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
                SendGrid checkedSendGrid = connectionTemplate.executeWithRetry(
                        "SendGrid - " + sendgrid.getAlias(),
                        () -> {
                            SendGrid sendGrid = new SendGrid(sendgrid.getApiKey());
                            try {
                                Request request = new Request();
                                request.setMethod(Method.GET);
                                request.setEndpoint("scopes");

                                Response response = sendGrid.api(request);
                                if (response.getStatusCode() == 401 || response.getStatusCode() == 403) {
                                    throw new DefaultFrameworkException("SendGrid Authentication failed. Invalid API Key. " + response.getBody());
                                } else if (response.getStatusCode() >= 500) {
                                    throw new DefaultFrameworkException("SendGrid Server Error: " + response.getBody());
                                }

                                return sendGrid;
                            } catch (IOException e) {
                                throw new DefaultFrameworkException(e.getMessage());
                            }
                        }
                );
                sendGridMap.put(new AliasEnv(sendgrid.getAlias(), env), checkedSendGrid);
            }
        }
    }
}
