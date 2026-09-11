package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.model.scenario.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailExecutorTest {

    private static final String DEV_ENV = "dev";
    private static final String TEST_ALIAS = "mainInbox";

    @Mock
    private EmailInboxService emailInboxService;

    @Mock
    private ApplicationContext context;

    private ScenarioContext scenarioContext;
    private Map<AliasEnv, EmailInboxService> servicesMap;
    private EmailExecutor executor;

    @BeforeEach
    void setUp() {
        this.servicesMap = new HashMap<>();
        this.servicesMap.put(new AliasEnv(TEST_ALIAS, DEV_ENV), this.emailInboxService);

        when(this.context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(this.context.containsBean("emailInboxServices")).thenReturn(true);
        when(this.context.getBean("emailInboxServices", Map.class)).thenReturn(this.servicesMap);

        this.scenarioContext = new ScenarioContext(new HashMap<>());
        final ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(this.context)
                .scenarioContext(this.scenarioContext)
                .environment(DEV_ENV)
                .build();

        this.executor = new EmailExecutor(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void buildsSuccessfully() {
            assertNotNull(EmailExecutorTest.this.executor);
        }

        @Test
        void buildsWhenBeanMissing() {
            final ApplicationContext emptyCtx = mock(ApplicationContext.class);
            when(emptyCtx.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
            when(emptyCtx.containsBean("emailInboxServices")).thenReturn(false);

            final ExecutorDependencies deps = ExecutorDependencies.builder()
                    .context(emptyCtx)
                    .scenarioContext(EmailExecutorTest.this.scenarioContext)
                    .environment(DEV_ENV)
                    .build();

            assertNotNull(new EmailExecutor(deps));
        }
    }

    @Nested
    class Execution {

        @Test
        void extractsValueAndSetsTargetVariable() {
            final Email email = new Email();
            email.setAlias(TEST_ALIAS);
            email.setPattern("otp: (\\d+)");
            email.setTimeout(BigInteger.valueOf(5000L));
            email.setTargetVariable("savedOtp");

            when(EmailExecutorTest.this.emailInboxService.fetchValueByPattern("otp: (\\d+)", 5000L))
                    .thenReturn("777888");

            final CommandResult result = new CommandResult();
            EmailExecutorTest.this.executor.execute(email, result);

            verify(EmailExecutorTest.this.emailInboxService).fetchValueByPattern("otp: (\\d+)", 5000L);
            assertEquals("777888", EmailExecutorTest.this.scenarioContext.get("savedOtp"));
            assertEquals(TEST_ALIAS, result.getMetadata().get("Alias"));
            assertEquals("otp: (\\d+)", result.getMetadata().get("Pattern"));
            assertEquals(BigInteger.valueOf(5000L), result.getMetadata().get("Timeout"));
            assertEquals("savedOtp", result.getMetadata().get("Target Variable"));
            assertEquals("777888", result.getMetadata().get("Extracted Value"));
            assertEquals("777888", result.getActual());
        }

        @Test
        void extractsValueWithoutTargetVariable() {
            final Email email = new Email();
            email.setAlias(TEST_ALIAS);
            email.setPattern("pin: (\\d+)");
            email.setTimeout(BigInteger.valueOf(3000L));

            when(EmailExecutorTest.this.emailInboxService.fetchValueByPattern("pin: (\\d+)", 3000L))
                    .thenReturn("1234");

            final CommandResult result = new CommandResult();
            EmailExecutorTest.this.executor.execute(email, result);

            assertFalse(EmailExecutorTest.this.scenarioContext.containsKey("savedOtp"));
            assertEquals("1234", result.getMetadata().get("Extracted Value"));
            assertEquals("1234", result.getActual());
        }

        @Test
        void appliesDefaultAliasWhenNull() {
            final Email email = new Email();
            email.setAlias(null);
            email.setPattern(".*");
            email.setTimeout(BigInteger.valueOf(1000L));

            EmailExecutorTest.this.servicesMap.put(
                    new AliasEnv("DEFAULT", DEV_ENV), EmailExecutorTest.this.emailInboxService);
            when(EmailExecutorTest.this.emailInboxService.fetchValueByPattern(".*", 1000L))
                    .thenReturn("found");

            final CommandResult result = new CommandResult();
            EmailExecutorTest.this.executor.execute(email, result);

            assertEquals("DEFAULT", result.getMetadata().get("Alias"));
            assertEquals("found", result.getActual());
        }

        @Test
        void throwsExceptionWhenServiceNotFound() {
            final Email email = new Email();
            email.setAlias("nonExistent");
            email.setPattern(".*");

            final CommandResult result = new CommandResult();
            final DefaultFrameworkException ex = assertThrows(
                    DefaultFrameworkException.class,
                    () -> EmailExecutorTest.this.executor.execute(email, result));

            assertEquals("Email inbox configuration not found for alias 'nonExistent' and environment 'dev'",
                    ex.getMessage());
        }
    }
}
