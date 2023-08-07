package com.knubisoft.testlum.testing.framework.configuration.smtp;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSmtpEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Smtp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@Conditional(OnSmtpEnabledCondition.class)
public class SmtpConfiguration {

    private static final String SMTP_PROTOCOL = "smtp";
    @Autowired
    private GlobalTestConfigurationProvider globalTestConfigurationProvider;

    @Bean
    public Map<AliasEnv, JavaMailSenderImpl> javaMailSender() {
        Map<AliasEnv, JavaMailSenderImpl> senderMap = new HashMap<>();
        globalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addSenderToMap(integrations, env, senderMap));
        return senderMap;
    }

    private void addSenderToMap(final Integrations integrations,
                                final String env,
                                final Map<AliasEnv, JavaMailSenderImpl> senderMap) {
        for (Smtp smtp : integrations.getSmtpIntegration().getSmtp()) {
            if (smtp.isEnabled()) {
                JavaMailSenderImpl javaMailSender = createJavaMailSender(smtp);
                senderMap.put(new AliasEnv(smtp.getAlias(), env), javaMailSender);
            }
        }
    }

    private JavaMailSenderImpl createJavaMailSender(final Smtp smtp) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(smtp.getHost());
        javaMailSender.setPort(smtp.getPort().intValue());
        javaMailSender.setUsername(smtp.getUsername());
        javaMailSender.setPassword(smtp.getPassword());
        setSMTPProperties(javaMailSender.getJavaMailProperties(), smtp);
        return javaMailSender;
    }

    private void setSMTPProperties(final Properties properties, final Smtp smtpSettings) {
        properties.put("mail.transport.protocol", SMTP_PROTOCOL);
        properties.put("mail.smtp.auth", smtpSettings.isSmtpAuth());
        properties.put("mail.smtp.starttls.enable", smtpSettings.isSmtpStarttlsEnable());
    }
}
