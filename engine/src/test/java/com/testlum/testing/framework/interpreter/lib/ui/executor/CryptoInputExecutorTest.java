package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.UiUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.global_config.CryptoMethods;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.scenario.CryptoInput;
import com.testlum.testing.model.scenario.CryptoOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoInputExecutorTest {

    private static final String TEST_ALIAS = "TEST_ALIAS";
    private static final String TEST_ENV = "test_env";

    @Mock
    private UiUtil uiUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;
    @Mock
    private CryptographyService cryptographyService;

    private CryptoInputExecutor executor;
    private File scenarioFile;
    private Map<AliasEnv, Cryptography> cryptographyIntegrations;

    @BeforeEach
    void setUp() {
        scenarioFile = mock(File.class);
        mockApplicationContext();

        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(scenarioFile)
                .environment(TEST_ENV)
                .build();

        executor = new CryptoInputExecutor(dependencies);
        setField(executor, "uiUtil", uiUtil);

        cryptographyIntegrations = new HashMap<>();
        setField(executor, "cryptographyIntegrations", cryptographyIntegrations);
    }

    private void mockApplicationContext() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> {
            Class<?> clazz = inv.getArgument(0);
            if (clazz.equals(CryptographyService.class)) {
                return cryptographyService;
            }
            if (clazz.equals(UiUtil.class)) {
                return uiUtil;
            }
            return mock(clazz);
        });
    }

    @Nested
    @DisplayName("Execute tests")
    class Execute {

        @Test
        @DisplayName("Should successfully process crypto input, send keys to element, and record metadata")
        void shouldProcessCryptoInputAndSendKeysToElement() {
            CryptoInput cryptoInput = createCryptoInput("rawSecretValue", TEST_ALIAS);
            WebElement webElement = mock(WebElement.class);

            addCryptoIntegrationToMap(TEST_ALIAS, TEST_ENV, "AES", "superSecretKey123");
            stubCryptoServices(webElement);

            CommandResult result = new CommandResult();
            executor.execute(cryptoInput, result);

            verify(webElement).sendKeys("encryptedValue123");
            verify(uiUtil).highlightElementIfRequired(eq(true), eq(webElement), eq(driver));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());

            assertEquals("crypto_field_locator", result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
            assertEquals("encryptedValue123", result.getMetadata().get(ResultUtil.INPUT_VALUE));
        }

        @Test
        @DisplayName("Should throw DefaultFrameworkException when cryptography is not configured for alias and env")
        void shouldThrowExceptionWhenCryptoNotConfigured() {
            CryptoInput cryptoInput = createCryptoInput("rawSecretValue", "UNKNOWN_ALIAS");

            CommandResult result = new CommandResult();
            DefaultFrameworkException ex = assertThrows(
                    DefaultFrameworkException.class, () -> executor.execute(cryptoInput, result));

            String expectedMessage = String.format(
                    "Cryptography integration is not configured for alias <UNKNOWN_ALIAS> and env <%s>", TEST_ENV
            );

            assertEquals(expectedMessage, ex.getMessage());
            assertEquals("crypto_field_locator", result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
        }

        @Test
        @DisplayName("Should throw NullPointerException when operation is missing")
        void shouldThrowExceptionWhenOperationIsMissing() {
            CryptoInput cryptoInput = mock(CryptoInput.class);
            when(cryptoInput.getLocator()).thenReturn("crypto_field_locator");

            CommandResult result = new CommandResult();

            assertThrows(NullPointerException.class, () -> executor.execute(cryptoInput, result));
            assertEquals("crypto_field_locator", result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
        }

        private CryptoInput createCryptoInput(final String secretValue, final String alias) {
            CryptoOperation operation = mock(CryptoOperation.class);
            lenient().when(operation.getValue()).thenReturn(secretValue);
            lenient().when(operation.getAlias()).thenReturn(alias);

            CryptoInput cryptoInput = mock(CryptoInput.class);
            lenient().when(cryptoInput.getLocator()).thenReturn("crypto_field_locator");
            lenient().when(cryptoInput.getAction()).thenReturn("ENCRYPT");
            lenient().when(cryptoInput.getOperation()).thenReturn(operation);
            lenient().when(cryptoInput.isHighlight()).thenReturn(true);
            return cryptoInput;
        }

        private void stubCryptoServices(final WebElement webElement) {
            when(cryptographyService.processCommand("rawSecretValue", "ENCRYPT",
                    "AES", "superSecretKey123", TEST_ALIAS))
                    .thenReturn("encryptedValue123");

            when(uiUtil.findWebElement(
                    any(), eq("crypto_field_locator"), any(), eq(ElementChecks.FOR_WRITING)
            )).thenReturn(webElement);

            when(uiUtil.resolveSendKeysType(eq("encryptedValue123"), eq(webElement), eq(scenarioFile)))
                    .thenReturn("encryptedValue123");
        }
    }

    private void addCryptoIntegrationToMap(
            final String alias, final String env, final String method, final String secret
    ) {
        final Cryptography cryptoConfig = new Cryptography();
        cryptoConfig.setSecret(secret);
        cryptoConfig.setMethod(CryptoMethods.valueOf(method));

        cryptographyIntegrations.put(new AliasEnv(alias, env), cryptoConfig);
    }

    private void setField(final Object target, final String fieldName, final Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field value via reflection", e);
            }
        }
        throw new RuntimeException("Field '" + fieldName + "' not found in class hierarchy of " + target.getClass());
    }
}