package com.knubisoft.cott.testing.framework.configuration.sendgrid;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Sendgrid;
import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Configuration
@Conditional({OnSendgridEnabledCondition.class})
public class SendgridConfiguration {

    @Bean
    public Map<String, SendGrid> sendGrid() {
        Map<String, SendGrid> sendGridMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToMap(s, integrations.getSendgridIntegration().getSendgrid(),
                        sendGridMap)));
    return sendGridMap;
    }

    public void addToMap(final String envName,
                         final List<Sendgrid> sendgrids,
                         final Map<String, SendGrid> sendGridMap) {
        sendgrids.stream()
                .filter(Sendgrid::isEnabled)
                .forEach(sendgrid -> sendGridMap.put(envName + UNDERSCORE + sendgrid.getAlias(),
                        new SendGrid(sendgrid.getApiKey())));
    }
}
