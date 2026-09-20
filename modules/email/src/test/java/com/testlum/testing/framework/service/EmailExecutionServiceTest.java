package com.testlum.testing.framework.service;

import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.StringPrettifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailExecutionServiceTest {

    private static final String DEV = "dev";
    private static final String ALIAS = "inbox";

    @Mock
    private StringPrettifier stringPrettifier;

    @Mock
    private EmailInboxService service;

    private EmailExecutionService emailExecutionService;
    private Map<AliasEnv, EmailInboxService> services;

    @BeforeEach
    void setUp() {
        this.emailExecutionService = new EmailExecutionService(this.stringPrettifier);
        this.services = new HashMap<>();
        this.services.put(new AliasEnv(ALIAS, DEV), this.service);
    }

    @Nested
    class ResolveService {

        @Test
        void returnsServiceWhenFound() {
            final EmailInboxService result = emailExecutionService.resolveService(services, ALIAS, DEV);
            assertNotNull(result);
            assertEquals(service, result);
        }

        @Test
        void throwsWhenNotFound() {
            final DefaultFrameworkException ex = assertThrows(
                    DefaultFrameworkException.class,
                    () -> emailExecutionService.resolveService(services, "missing", DEV));
            assertEquals("Email inbox configuration not found for alias 'missing' and environment 'dev'",
                    ex.getMessage());
        }

        @Test
        void throwsWhenMapIsNull() {
            assertThrows(DefaultFrameworkException.class,
                    () -> emailExecutionService.resolveService(null, ALIAS, DEV));
        }
    }

    @Nested
    class HandleTargetVariable {

        @Test
        void setsVariableWhenNotBlank() {
            final ScenarioContext context = new ScenarioContext(new HashMap<>());
            emailExecutionService.handleTargetVariable(context, "myVar", "12345");
            assertEquals("12345", context.get("myVar"));
        }

        @Test
        void doesNothingWhenNullOrBlank() {
            final ScenarioContext context = new ScenarioContext(new HashMap<>());
            emailExecutionService.handleTargetVariable(context, null, "12345");
            emailExecutionService.handleTargetVariable(context, "", "12345");
            emailExecutionService.handleTargetVariable(context, "   ", "12345");
            assertFalse(context.containsKey("myVar"));
        }
    }

    @Nested
    class LogEmailInfo {

        @Test
        void logsWithTargetVariable() {
            when(stringPrettifier.cut("999")).thenReturn("999");
            emailExecutionService.logEmailInfo(ALIAS, "\\d+", BigInteger.valueOf(5000), "targetVar", "999");
        }

        @Test
        void logsWithoutTargetVariable() {
            when(stringPrettifier.cut("999")).thenReturn("999");
            emailExecutionService.logEmailInfo(ALIAS, "\\d+", BigInteger.valueOf(5000), null, "999");
        }
    }

    @Nested
    class AddEmailMetaData {

        @Test
        void populatesResultWithTargetVariable() {
            final CommandResult result = new CommandResult();
            emailExecutionService.addEmailMetaData(result, ALIAS, "\\d+", BigInteger.valueOf(5000), "targetVar", "val");
            assertEquals(ALIAS, result.getMetadata().get("Alias"));
            assertEquals("\\d+", result.getMetadata().get("Pattern"));
            assertEquals(BigInteger.valueOf(5000), result.getMetadata().get("Timeout"));
            assertEquals("targetVar", result.getMetadata().get("Target Variable"));
            assertEquals("val", result.getMetadata().get("Extracted Value"));
            assertEquals("val", result.getActual());
        }

        @Test
        void populatesResultWithoutTargetVariable() {
            final CommandResult result = new CommandResult();
            emailExecutionService.addEmailMetaData(result, ALIAS, "\\d+", BigInteger.valueOf(3000), null, "val");
            assertEquals(ALIAS, result.getMetadata().get("Alias"));
            assertFalse(result.getMetadata().containsKey("Target Variable"));
            assertEquals("val", result.getActual());
        }
    }
}
