package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailInterpreterTest {

    private static final String DEV_ENV = "dev";
    private static final String TEST_ALIAS = "mainInbox";

    @Mock
    private EmailInboxService emailInboxService;

    @Mock
    private JacksonService jacksonService;

    @Mock
    private ConditionProvider conditionProvider;

    @Mock
    private StringPrettifier stringPrettifier;

    private ApplicationContext applicationContext;
    private ScenarioContext scenarioContext;
    private Map<AliasEnv, EmailInboxService> servicesMap;
    private EmailInterpreter interpreter;

    @BeforeEach
    void setUp() {
        this.servicesMap = new HashMap<>();
        this.servicesMap.put(new AliasEnv(TEST_ALIAS, DEV_ENV), this.emailInboxService);

        this.applicationContext = mock(ApplicationContext.class);
        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        when(this.applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(this.applicationContext.getBean(ConditionProvider.class)).thenReturn(this.conditionProvider);
        when(this.applicationContext.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(this.applicationContext.getBean(JacksonService.class)).thenReturn(this.jacksonService);
        when(this.applicationContext.getBean(StringPrettifier.class)).thenReturn(this.stringPrettifier);
        when(this.applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        when(this.applicationContext.containsBean("emailInboxServices")).thenReturn(true);
        when(this.applicationContext.getBean("emailInboxServices", Map.class)).thenReturn(this.servicesMap);

        this.scenarioContext = new ScenarioContext(new HashMap<>());
        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(this.applicationContext)
                .file(new File("scenario.xml"))
                .scenarioContext(this.scenarioContext)
                .position(new AtomicInteger(1))
                .environment(DEV_ENV)
                .build();

        this.interpreter = new EmailInterpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void buildsSuccessfullyWithProvidedServices() {
            assertNotNull(EmailInterpreterTest.this.interpreter);
        }

        @Test
        void buildsSuccessfullyWhenBeanIsMissing() {
            final ApplicationContext emptyContext = mock(ApplicationContext.class);
            final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
            when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
            when(emptyContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
            when(emptyContext.getBean(ConditionProvider.class)).thenReturn(EmailInterpreterTest.this.conditionProvider);
            when(emptyContext.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
            when(emptyContext.getBean(JacksonService.class)).thenReturn(EmailInterpreterTest.this.jacksonService);
            when(emptyContext.getBean(StringPrettifier.class)).thenReturn(EmailInterpreterTest.this.stringPrettifier);
            when(emptyContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
            when(emptyContext.containsBean("emailInboxServices")).thenReturn(false);

            final InterpreterDependencies deps = InterpreterDependencies.builder()
                    .context(emptyContext)
                    .file(new File("scenario.xml"))
                    .scenarioContext(EmailInterpreterTest.this.scenarioContext)
                    .position(new AtomicInteger(1))
                    .environment(DEV_ENV)
                    .build();

            final EmailInterpreter fallbackInterpreter = new EmailInterpreter(deps);
            assertNotNull(fallbackInterpreter);
        }
    }

    @Nested
    class Execution {

        @Test
        void extractsValueAndSetsTargetVariable() {
            final Email email = createTestEmail(TEST_ALIAS, "code: (\\d+)", 5000L, "otpCode");
            mockInjectionAndCondition(email);
            when(EmailInterpreterTest.this.emailInboxService.fetchValueByPattern("code: (\\d+)", 5000L))
                    .thenReturn("849201");
            when(EmailInterpreterTest.this.stringPrettifier.cut("849201")).thenReturn("849201");

            final CommandResult result = new CommandResult();
            EmailInterpreterTest.this.interpreter.apply(email, result);

            verify(EmailInterpreterTest.this.emailInboxService).fetchValueByPattern("code: (\\d+)", 5000L);
            assertEquals("849201", EmailInterpreterTest.this.scenarioContext.get("otpCode"));
            assertEquals(TEST_ALIAS, result.getMetadata().get("Alias"));
            assertEquals("code: (\\d+)", result.getMetadata().get("Pattern"));
            assertEquals(BigInteger.valueOf(5000L), result.getMetadata().get("Timeout"));
            assertEquals("otpCode", result.getMetadata().get("Target Variable"));
            assertEquals("849201", result.getMetadata().get("Extracted Value"));
            assertEquals("849201", result.getActual());
        }

        @Test
        void extractsValueWithoutTargetVariable() {
            final Email email = createTestEmail(TEST_ALIAS, "token: (\\w+)", 3000L, null);
            mockInjectionAndCondition(email);
            when(EmailInterpreterTest.this.emailInboxService.fetchValueByPattern("token: (\\w+)", 3000L))
                    .thenReturn("abcXYZ");
            when(EmailInterpreterTest.this.stringPrettifier.cut("abcXYZ")).thenReturn("abcXYZ");

            final CommandResult result = new CommandResult();
            EmailInterpreterTest.this.interpreter.apply(email, result);

            assertFalse(EmailInterpreterTest.this.scenarioContext.containsKey("token"));
            assertEquals("abcXYZ", result.getMetadata().get("Extracted Value"));
            assertEquals("abcXYZ", result.getActual());
        }

        @Test
        void appliesDefaultAliasWhenAliasIsNull() {
            final Email email = createTestEmail(null, "\\d{4}", 2000L, null);
            mockInjectionAndCondition(email);
            EmailInterpreterTest.this.servicesMap.put(
                    new AliasEnv("DEFAULT", DEV_ENV), EmailInterpreterTest.this.emailInboxService);
            when(EmailInterpreterTest.this.emailInboxService.fetchValueByPattern("\\d{4}", 2000L))
                    .thenReturn("9999");
            when(EmailInterpreterTest.this.stringPrettifier.cut("9999")).thenReturn("9999");

            final CommandResult result = new CommandResult();
            EmailInterpreterTest.this.interpreter.apply(email, result);

            assertEquals("DEFAULT", result.getMetadata().get("Alias"));
            assertEquals("9999", result.getActual());
        }

        @Test
        void throwsExceptionWhenServiceNotFound() {
            final Email email = createTestEmail("unknownAlias", ".*", 1000L, null);
            mockInjectionAndCondition(email);

            final CommandResult result = new CommandResult();
            final DefaultFrameworkException ex = assertThrows(
                    DefaultFrameworkException.class,
                    () -> EmailInterpreterTest.this.interpreter.apply(email, result));

            assertEquals("Email inbox configuration not found for alias 'unknownAlias' and environment 'dev'",
                    ex.getMessage());
        }

        private Email createTestEmail(final String alias,
                                      final String pattern,
                                      final Long timeout,
                                      final String targetVariable) {
            final Email email = new Email();
            email.setAlias(alias);
            email.setPattern(pattern);
            if (timeout != null) {
                email.setTimeout(BigInteger.valueOf(timeout));
            }
            email.setTargetVariable(targetVariable);
            return email;
        }

        private void mockInjectionAndCondition(final Email email) {
            when(EmailInterpreterTest.this.conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
            when(EmailInterpreterTest.this.jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
            when(EmailInterpreterTest.this.jacksonService.readCopiedValue(anyString(), eq(Email.class)))
                    .thenReturn(email);
        }
    }
}
