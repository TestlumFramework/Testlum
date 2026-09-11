package com.testlum.testing.framework.email;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.interpreter.EmailInterpreter;
import com.testlum.testing.framework.interpreter.lib.CommandToInterpreterClassMap;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterProvider;
import com.testlum.testing.framework.interpreter.lib.InterpreterScanner;
import com.testlum.testing.framework.interpreter.lib.ui.CommandToExecutorClassMap;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorProvider;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorScanner;
import com.testlum.testing.framework.interpreter.lib.ui.executor.EmailExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.executor.InputEmailExecutor;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.service.EmailInboxService;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.framework.util.UiUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Email;
import com.testlum.testing.model.scenario.InputEmail;
import com.testlum.testing.model.scenario.Scenario;
import com.testlum.testing.model.scenario.Web;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailE2ETest {

    private static final String DEV = "dev";
    private static final String INBOX_ALIAS = "mainInbox";
    private static final String OTP_VALUE = "481920";

    @Mock
    private EmailInboxService emailInboxService;

    @Mock
    private UiUtil uiUtil;

    @Mock
    private WebDriver driver;

    @Mock
    private ApplicationContext context;

    private ScenarioContext scenarioContext;
    private Map<AliasEnv, EmailInboxService> servicesMap;

    @BeforeEach
    void setUp() {
        this.servicesMap = new HashMap<>();
        this.servicesMap.put(new AliasEnv(INBOX_ALIAS, DEV), this.emailInboxService);

        final JacksonService jackson = new JacksonService();
        final StringPrettifier prettifier = new StringPrettifier(jackson);
        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        lenient().when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        lenient().when(this.context.getBean(JacksonService.class)).thenReturn(jackson);
        lenient().when(this.context.getBean(StringPrettifier.class)).thenReturn(prettifier);
        lenient().when(this.context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        lenient().when(this.context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        lenient().when(this.context.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        lenient().when(this.context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        lenient().when(this.context.containsBean("emailInboxServices")).thenReturn(true);
        lenient().when(this.context.getBean("emailInboxServices", Map.class)).thenReturn(this.servicesMap);

        this.scenarioContext = new ScenarioContext(new HashMap<>());
    }

    @Nested
    class DiscoveryAndLifecycle {

        @Test
        void scannerAndProviderResolveInterpreter() {
            final InterpreterScanner scanner = new InterpreterScanner();
            final CommandToInterpreterClassMap map = scanner.getInterpreters();
            assertEquals(EmailInterpreter.class, map.get(Email.class));

            final InterpreterProvider provider = new InterpreterProvider(scanner);
            provider.init();
            final InterpreterDependencies deps = createInterpreterDependencies();
            final Object instance = provider.getAppropriateInterpreter(new Email(), deps);
            assertInstanceOf(EmailInterpreter.class, instance);
        }

        @Test
        void scannerAndProviderResolveExecutors() {
            final ExecutorScanner scanner = new ExecutorScanner();
            final CommandToExecutorClassMap map = scanner.getExecutors();
            assertEquals(EmailExecutor.class, map.get(Email.class));
            assertEquals(InputEmailExecutor.class, map.get(InputEmail.class));

            final ExecutorProvider provider = new ExecutorProvider(scanner);
            provider.init();
            final ExecutorDependencies deps = createExecutorDependencies();
            assertInstanceOf(EmailExecutor.class, provider.getAppropriateExecutor(new Email(), deps));
            assertInstanceOf(InputEmailExecutor.class, provider.getAppropriateExecutor(new InputEmail(), deps));
        }
    }

    @Nested
    class XmlBinding {

        @Test
        void unmarshalsScenarioWithEmailAndWebInputEmail() throws Exception {
            final String xml = """
                    <scenario xmlns="http://www.testlum.com/testing/model/scenario">
                        <overview><name>Email E2E</name></overview>
                        <settings><tags>e2e</tags></settings>
                        <email alias="mainInbox" pattern="OTP: (\\d{6})" targetVariable="otpCode"/>
                        <web>
                            <inputEmail locator="//input[@id='otp']" pattern="OTP: (\\d{6})" alias="mainInbox"/>
                        </web>
                    </scenario>
                    """;
            final JAXBContext jaxbContext = JAXBContext.newInstance(Scenario.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final Scenario scenario = (Scenario) unmarshaller.unmarshal(new StringReader(xml));

            assertNotNull(scenario);
            assertEquals(2, scenario.getCommands().size());
            final Email email = (Email) scenario.getCommands().get(0);
            assertEquals("mainInbox", email.getAlias());
            assertEquals("OTP: (\\d{6})", email.getPattern());
            assertEquals("otpCode", email.getTargetVariable());

            final Web web = (Web) scenario.getCommands().get(1);
            assertEquals(1, web.getClickOrInputOrAssert().size());
            final InputEmail inputEmail = (InputEmail) web.getClickOrInputOrAssert().get(0);
            assertEquals("//input[@id='otp']", inputEmail.getLocator());
        }
    }

    @Nested
    class ExecutionFlow {

        @Test
        void runsStandaloneEmailThenUiInputEmailStep() {
            when(EmailE2ETest.this.emailInboxService.fetchValueByPattern(eq("OTP: (\\d{6})"), eq(10000L)))
                    .thenReturn(OTP_VALUE);

            final EmailInterpreter interpreter = new EmailInterpreter(createInterpreterDependencies());
            final Email email = new Email();
            email.setAlias(INBOX_ALIAS);
            email.setPattern("OTP: (\\d{6})");
            email.setTimeout(BigInteger.valueOf(10000L));
            email.setTargetVariable("otpCode");

            final CommandResult standaloneResult = new CommandResult();
            ReflectionTestUtils.invokeMethod(interpreter, "acceptImpl", email, standaloneResult);
            assertEquals(OTP_VALUE, EmailE2ETest.this.scenarioContext.get("otpCode"));

            final InputEmailExecutor inputExecutor = new InputEmailExecutor(createExecutorDependencies());
            ReflectionTestUtils.setField(inputExecutor, "uiUtil", EmailE2ETest.this.uiUtil);

            final InputEmail inputEmail = new InputEmail();
            inputEmail.setLocator("//input[@id='otp']");
            inputEmail.setAlias(INBOX_ALIAS);
            inputEmail.setPattern("OTP: (\\d{6})");
            inputEmail.setTimeout(BigInteger.valueOf(10000L));
            inputEmail.setHighlight(true);

            final WebElement element = mock(WebElement.class);
            when(EmailE2ETest.this.uiUtil.findWebElement(any(), eq("//input[@id='otp']"), any(),
                    eq(ElementChecks.FOR_WRITING))).thenReturn(element);

            final CommandResult uiResult = new CommandResult();
            ReflectionTestUtils.invokeMethod(inputExecutor, "execute", inputEmail, uiResult);

            verify(element).sendKeys(OTP_VALUE);
            verify(EmailE2ETest.this.uiUtil).highlightElementIfRequired(
                    eq(true), eq(element), eq(EmailE2ETest.this.driver));
            assertEquals(OTP_VALUE, uiResult.getMetadata().get(ResultUtil.INPUT_VALUE));
        }
    }

    private InterpreterDependencies createInterpreterDependencies() {
        return InterpreterDependencies.builder()
                .context(this.context)
                .scenarioContext(this.scenarioContext)
                .environment(DEV)
                .build();
    }

    private ExecutorDependencies createExecutorDependencies() {
        return ExecutorDependencies.builder()
                .context(this.context)
                .scenarioContext(this.scenarioContext)
                .driver(this.driver)
                .environment(DEV)
                .build();
    }
}
