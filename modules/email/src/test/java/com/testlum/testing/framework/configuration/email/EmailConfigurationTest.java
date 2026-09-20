package com.testlum.testing.framework.configuration.email;

import com.testlum.testing.connection.ConnectionTemplate;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.model.global_config.Email;
import com.testlum.testing.model.global_config.EmailIntegration;
import com.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailConfigurationTest {

    @Mock
    private ConnectionTemplate connectionTemplate;

    private EmailConfiguration configuration;

    @BeforeEach
    void setUp() {
        this.configuration = new EmailConfiguration(this.connectionTemplate);
    }

    @Nested
    class EmailInboxServices {

        @Test
        void registersEnabledEmailForEnvironment() {
            when(connectionTemplate.executeWithRetry(anyString(), any(), any()))
                    .thenAnswer(invocation -> invocation.getArgument(1, Supplier.class).get());

            final Integrations integrations = new Integrations();
            final EmailIntegration emailIntegration = new EmailIntegration();
            final Email email = new Email();
            email.setAlias("mainInbox");
            email.setEnabled(true);
            emailIntegration.getEmail().add(email);
            integrations.setEmailIntegration(emailIntegration);

            final EnvToIntegrationMap envMap = new EnvToIntegrationMap(Map.of("dev", integrations));

            final Map<AliasEnv, EmailInboxService> result = configuration.emailInboxServices(envMap);

            assertEquals(1, result.size());
            final AliasEnv key = new AliasEnv("mainInbox", "dev");
            assertTrue(result.containsKey(key));
            assertNotNull(result.get(key));
            assertEquals("mainInbox", result.get(key).getEmailSettings().getAlias());
        }

        @Test
        void ignoresDisabledEmail() {
            final Integrations integrations = new Integrations();
            final EmailIntegration emailIntegration = new EmailIntegration();
            final Email email = new Email();
            email.setAlias("disabledInbox");
            email.setEnabled(false);
            emailIntegration.getEmail().add(email);
            integrations.setEmailIntegration(emailIntegration);

            final EnvToIntegrationMap envMap = new EnvToIntegrationMap(Map.of("dev", integrations));

            final Map<AliasEnv, EmailInboxService> result = configuration.emailInboxServices(envMap);

            assertTrue(result.isEmpty());
        }

        @Test
        void handlesNullEmailIntegration() {
            final Integrations integrations = new Integrations();
            final EnvToIntegrationMap envMap = new EnvToIntegrationMap(Map.of("dev", integrations));

            final Map<AliasEnv, EmailInboxService> result = configuration.emailInboxServices(envMap);

            assertTrue(result.isEmpty());
        }
    }
}
