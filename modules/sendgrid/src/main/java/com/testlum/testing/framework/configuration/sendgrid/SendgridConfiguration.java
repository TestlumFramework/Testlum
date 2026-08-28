package com.testlum.testing.framework.configuration.sendgrid;

import com.testlum.testing.connection.ConnectionTemplate;
import com.testlum.testing.connection.IntegrationHealthCheck;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.condition.OnSendgridEnabledCondition;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Sendgrid;
import com.sendgrid.Method;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSendgridEnabledCondition.class})
@RequiredArgsConstructor
public class SendgridConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean("sendGridMap")
    public Map<AliasEnv, SendGrid> sendGrid(final EnvToIntegrationMap envToIntegrations) {
        Map<AliasEnv, SendGrid> sendGridMap = new HashMap<>();
        envToIntegrations.forEach((env, integrations) -> addToMap(integrations, env, sendGridMap));
        return sendGridMap;
    }

    public void addToMap(final Integrations integrations,
                         final String env,
                         final Map<AliasEnv, SendGrid> sendGridMap) {
        for (Sendgrid sendgrid : integrations.getSendgridIntegration().getSendgrid()) {
            if (sendgrid.isEnabled()) {
                SendGrid checkedSendGrid = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "SendGrid", sendgrid.getAlias()),
                        () -> new SendGrid(sendgrid.getApiKey()),
                        forSendGrid()
                );
                sendGridMap.put(new AliasEnv(sendgrid.getAlias(), env), checkedSendGrid);
            }
        }
    }

    private IntegrationHealthCheck<SendGrid> forSendGrid() {
        return sdkSendGrid -> {
            com.sendgrid.Request request = new com.sendgrid.Request();
            request.setMethod(Method.GET);
            request.setEndpoint("scopes");

            com.sendgrid.Response response = sdkSendGrid.api(request);
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED.value()
                    || response.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
                throw new DefaultFrameworkException("SendGrid Authentication failed. " + response.getBody());
            } else if (response.getStatusCode() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                throw new DefaultFrameworkException("SendGrid Server Error: " + response.getBody());
            }
        };
    }
}
