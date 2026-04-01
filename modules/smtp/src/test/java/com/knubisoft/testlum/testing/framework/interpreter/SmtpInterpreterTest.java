package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.Smtp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmtpInterpreterTest {

    @TempDir
    File tempDir;

    private SmtpInterpreter interpreter;

    @BeforeEach
    void setUp() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(applicationContext.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new SmtpInterpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(interpreter);
        }
    }

    @Nested
    class GetSimpleMailMessage {

        @Test
        void createsMessageWithAllFields() throws Exception {
            final Smtp smtp = new Smtp();
            smtp.setRecipientEmail("recipient@example.com");
            smtp.setSubject("Test Subject");
            smtp.setText("Test Body");

            final Method method = SmtpInterpreter.class.getDeclaredMethod(
                    "getSimpleMailMessage", Smtp.class, String.class);
            method.setAccessible(true);
            final org.springframework.mail.SimpleMailMessage message =
                    (org.springframework.mail.SimpleMailMessage) method.invoke(interpreter, smtp, "sender@example.com");

            assertEquals("sender@example.com", message.getFrom());
            assertNotNull(message.getTo());
            assertEquals("recipient@example.com", message.getTo()[0]);
            assertEquals("Test Subject", message.getSubject());
            assertEquals("Test Body", message.getText());
        }
    }

    @Nested
    class AddSmtpMetaData {

        @Test
        void addsAllMetadataToResult() throws Exception {
            final Smtp smtp = new Smtp();
            smtp.setAlias("smtpAlias");
            smtp.setRecipientEmail("dest@example.com");
            smtp.setSubject("Subject");
            smtp.setText("Body text");

            final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost("smtp.example.com");
            javaMailSender.setPort(587);
            javaMailSender.setUsername("user@example.com");

            final CommandResult result = new CommandResult();

            final Method method = SmtpInterpreter.class.getDeclaredMethod(
                    "addSmtpMetaData", Smtp.class, JavaMailSenderImpl.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(null, smtp, javaMailSender, result);

            assertEquals("smtpAlias", result.getMetadata().get("Alias"));
            assertEquals("smtp.example.com", result.getMetadata().get("SMTP Host"));
            assertEquals(587, result.getMetadata().get("SMTP Port"));
            assertEquals("user@example.com", result.getMetadata().get("Source"));
            assertEquals("dest@example.com", result.getMetadata().get("Destination"));
            assertEquals("Subject", result.getMetadata().get("Subject"));
            assertEquals("Body text", result.getMetadata().get("Text"));
        }
    }

    @Nested
    class AcceptImpl {

        @Test
        @SuppressWarnings("unchecked")
        void usesJavaMailSenderFromMap() throws Exception {
            final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost("smtp.test.com");
            javaMailSender.setPort(25);
            javaMailSender.setUsername("testuser@test.com");

            final Field field = SmtpInterpreter.class.getDeclaredField("javaMailSenderMap");
            field.setAccessible(true);
            final Map<Object, JavaMailSenderImpl> map = (Map<Object, JavaMailSenderImpl>) field.get(interpreter);
            assertNotNull(map);
        }
    }
}
