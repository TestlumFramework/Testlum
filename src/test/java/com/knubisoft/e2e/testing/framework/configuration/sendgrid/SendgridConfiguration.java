package com.knubisoft.e2e.testing.framework.configuration.sendgrid;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.Sendgrid;
import com.sendgrid.SendGrid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Conditional({OnSendgridEnabledCondition.class})
public class SendgridConfiguration {

    @Bean
    public Map<String, SendGrid> sendGrid() {
        return GlobalTestConfigurationProvider.provide().getSendgrids().getSendgrid().stream()
                .filter(Sendgrid::isEnabled)
                .collect(Collectors.toMap(Sendgrid::getAlias, o -> new SendGrid(o.getApiKey())));
    }
}
