package com.testlum.testing.framework.configuration.smtp;

import com.testlum.testing.connection.ConnectionTemplate;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Smtp;
import com.testlum.testing.model.global_config.SmtpIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.math.BigInteger;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SmtpConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
class SmtpConfigurationTest {

    @Mock
    private ConnectionTemplate connectionTemplate;

    private SmtpConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new SmtpConfiguration(connectionTemplate);
    }

    @Test
    @DisplayName("Should enable implicit SSL on port 465 and set connectiontimeout")
    void shouldEnableSslOnPort465AndSetConnectionTimeout() {
        when(connectionTemplate.executeWithRetry(anyString(), any(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());

        final Smtp smtp = new Smtp();
        smtp.setAlias("SMTP_465");
        smtp.setEnabled(true);
        smtp.setHost("smtp.ukr.net");
        smtp.setPort(BigInteger.valueOf(465));
        smtp.setUsername("user@ukr.net");
        smtp.setPassword("secret");
        smtp.setSmtpAuth(true);
        smtp.setSmtpStarttlsEnable(false);

        final SmtpIntegration smtpIntegration = new SmtpIntegration();
        smtpIntegration.getSmtp().add(smtp);

        final Integrations integrations = new Integrations();
        integrations.setSmtpIntegration(smtpIntegration);

        final EnvToIntegrationMap envToIntegrationMap = new EnvToIntegrationMap(Map.of("ci", integrations));

        final Map<AliasEnv, JavaMailSenderImpl> result = configuration.javaMailSender(envToIntegrationMap);

        final AliasEnv key = new AliasEnv("SMTP_465", "ci");
        assertTrue(result.containsKey(key));

        final JavaMailSenderImpl sender = result.get(key);
        assertNotNull(sender);
        assertEquals("smtp.ukr.net", sender.getHost());
        assertEquals(465, sender.getPort());

        final Properties props = sender.getJavaMailProperties();
        assertEquals(Boolean.TRUE, props.get("mail.smtp.ssl.enable"));
        assertEquals(5000, props.get("mail.smtp.connectiontimeout"));
        assertEquals(5000, props.get("mail.smtp.timeout"));
    }

    @Test
    @DisplayName("Should not enable SSL on port 587 but enable STARTTLS")
    void shouldNotEnableSslOnPort587() {
        when(connectionTemplate.executeWithRetry(anyString(), any(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());

        final Smtp smtp = new Smtp();
        smtp.setAlias("SMTP_587");
        smtp.setEnabled(true);
        smtp.setHost("smtp.gmail.com");
        smtp.setPort(BigInteger.valueOf(587));
        smtp.setUsername("user@gmail.com");
        smtp.setPassword("secret");
        smtp.setSmtpAuth(true);
        smtp.setSmtpStarttlsEnable(true);

        final SmtpIntegration smtpIntegration = new SmtpIntegration();
        smtpIntegration.getSmtp().add(smtp);

        final Integrations integrations = new Integrations();
        integrations.setSmtpIntegration(smtpIntegration);

        final EnvToIntegrationMap envToIntegrationMap = new EnvToIntegrationMap(Map.of("ci", integrations));

        final Map<AliasEnv, JavaMailSenderImpl> result = configuration.javaMailSender(envToIntegrationMap);

        final AliasEnv key = new AliasEnv("SMTP_587", "ci");
        assertTrue(result.containsKey(key));

        final JavaMailSenderImpl sender = result.get(key);
        final Properties props = sender.getJavaMailProperties();
        assertNull(props.get("mail.smtp.ssl.enable"));
        assertEquals(Boolean.TRUE, props.get("mail.smtp.starttls.enable"));
        assertEquals(5000, props.get("mail.smtp.connectiontimeout"));
    }

    @Test
    @DisplayName("Should ignore disabled SMTP configurations")
    void shouldIgnoreDisabledSmtp() {
        final Smtp smtp = new Smtp();
        smtp.setAlias("SMTP_DISABLED");
        smtp.setEnabled(false);

        final SmtpIntegration smtpIntegration = new SmtpIntegration();
        smtpIntegration.getSmtp().add(smtp);

        final Integrations integrations = new Integrations();
        integrations.setSmtpIntegration(smtpIntegration);

        final EnvToIntegrationMap envToIntegrationMap = new EnvToIntegrationMap(Map.of("ci", integrations));

        final Map<AliasEnv, JavaMailSenderImpl> result = configuration.javaMailSender(envToIntegrationMap);
        assertFalse(result.containsKey(new AliasEnv("SMTP_DISABLED", "ci")));
    }
}
