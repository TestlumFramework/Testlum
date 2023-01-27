package com.knubisoft.cott.testing.framework.configuration.smtp;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSmtpEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.Smtp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
@Conditional(OnSmtpEnabledCondition.class)
public class SmtpConfiguration {

    private static final String SMTP_PROTOCOL = "smtp";

    @Bean
    public Map<String, JavaMailSenderImpl> javaMailSender() {
        Map<String, JavaMailSenderImpl> senderMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToMap(s, integrations.getSmtpIntegration().getSmtp(), senderMap)));
        return senderMap;
    }

    private void addToMap(final String envName,
                          final List<Smtp> smtpList,
                          final Map<String, JavaMailSenderImpl> senderMap) {
        smtpList.stream()
                .filter(Smtp::isEnabled)
                .forEach(smtp -> senderMap.put(envName + DelimiterConstant.UNDERSCORE + smtp.getAlias(),
                        createJavaMailSender(smtp)));
    }

    private JavaMailSenderImpl createJavaMailSender(final Smtp smtpSettings) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(smtpSettings.getHost());
        javaMailSender.setPort(smtpSettings.getPort().intValue());
        javaMailSender.setUsername(smtpSettings.getUsername());
        javaMailSender.setPassword(smtpSettings.getPassword());
        setSMTPProperties(javaMailSender.getJavaMailProperties(), smtpSettings);
        return javaMailSender;
    }

    private void setSMTPProperties(final Properties properties,
                                   final Smtp smtpSettings) {
        properties.put("mail.transport.protocol", SMTP_PROTOCOL);
        properties.put("mail.smtp.auth", smtpSettings.isSmtpAuth());
        properties.put("mail.smtp.starttls.enable", smtpSettings.isSmtpStarttlsEnable());
    }
}
