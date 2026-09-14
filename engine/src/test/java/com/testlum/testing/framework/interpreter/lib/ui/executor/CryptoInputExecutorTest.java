package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.exception.IncorrectCryptographyValueException;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.IntegrationsProvider;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoInputExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;
    @Mock
    private CryptographyService cryptographyService;
    @Mock
    private IntegrationsProvider integrationsProvider;

    private CryptoInputExecutor executor;
    private File scenarioFile;

    @BeforeEach
    void setUp() {
        scenarioFile = mock(File.class);
        mockApplicationContext();

        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(scenarioFile)
                .build();

        executor = new CryptoInputExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    private void mockApplicationContext() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> {
            Class<?> clazz = inv.getArgument(0);
            if (clazz.equals(CryptographyService.class)) {
                return cryptographyService;
            }
            if (clazz.equals(IntegrationsProvider.class)) {
                return integrationsProvider;
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
        @DisplayName("Should successfully process crypto input,"
                + " send keys to element, and record metadata")
        void shouldProcessCryptoInputAndSendKeysToElement() {
            CryptoInput cryptoInput =
                    createCryptoInput("rawSecretValue", "TEST_ALIAS");
            WebElement webElement = mock(WebElement.class);
            stubCryptoServices(webElement);

            CommandResult result = new CommandResult();
            executor.execute(cryptoInput, result);

            verify(webElement).sendKeys("encryptedValue123");
            verify(uiUtil).highlightElementIfRequired(eq(true), eq(webElement), eq(driver));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());

            assertEquals("crypto_field_locator", result.getMetadata()
                    .get(ResultUtil.INPUT_LOCATOR));
            assertEquals("encryptedValue123", result.getMetadata()
                    .get(ResultUtil.INPUT_VALUE));
        }

        @Test
        @DisplayName("Should throw DefaultFrameworkException when operation is missing")
        void shouldThrowExceptionWhenOperationIsMissing() {
            CryptoInput cryptoInput = mock(CryptoInput.class);
            when(cryptoInput.getLocator()).thenReturn("crypto_field_locator");

            CommandResult result = new CommandResult();
            DefaultFrameworkException ex = assertThrows(
                    DefaultFrameworkException.class, () -> executor.execute(cryptoInput, result));

            assertTrue(ex.getMessage().contains("Crypto operation (<encrypt> or <decrypt>) is missing"));
            assertEquals("crypto_field_locator",
                    result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
        }

        @Test
        @DisplayName("Should throw IncorrectCryptographyValueException when value in operation is empty")
        void shouldThrowExceptionWhenOperationValueIsEmpty() {
            CryptoOperation operation = mock(CryptoOperation.class);
            when(operation.getValue()).thenReturn("   ");

            CryptoInput cryptoInput = mock(CryptoInput.class);
            when(cryptoInput.getLocator()).thenReturn("crypto_field_locator");
            when(cryptoInput.getOperation()).thenReturn(operation);

            final CommandResult result = new CommandResult();
            final IncorrectCryptographyValueException ex = assertThrows(
                    IncorrectCryptographyValueException.class, () -> executor.execute(cryptoInput, result));

            assertTrue(ex.getMessage().contains("Value must not be empty."));
            assertEquals("crypto_field_locator", result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
        }

        private CryptoInput createCryptoInput(final String secretValue, final String alias) {
            CryptoOperation operation = mock(CryptoOperation.class);
            when(operation.getValue()).thenReturn(secretValue);
            when(operation.getAlias()).thenReturn(alias);

            CryptoInput cryptoInput = mock(CryptoInput.class);
            when(cryptoInput.getLocator()).thenReturn("crypto_field_locator");
            when(cryptoInput.getAction()).thenReturn("ENCRYPT");
            when(cryptoInput.getOperation()).thenReturn(operation);
            when(cryptoInput.isHighlight()).thenReturn(true);
            return cryptoInput;
        }

        private void stubCryptoServices(final WebElement webElement) {
            Cryptography cryptography = new Cryptography();
            cryptography.setSecret("superSecretKey123");
            cryptography.setMethod(CryptoMethods.AES);
            cryptography.setAlias("TEST_ALIAS");

            List<Cryptography> list = Collections.singletonList(cryptography);
            when(integrationsProvider.findListByEnv(eq(Cryptography.class), any())).thenReturn(list);
            when(integrationsProvider.findCryptographyForAlias(list, "TEST_ALIAS")).thenReturn(cryptography);

            when(cryptographyService.processCommand("rawSecretValue", "ENCRYPT",
                    "AES", "superSecretKey123", "TEST_ALIAS"))
                    .thenReturn("encryptedValue123");

            when(uiUtil.findWebElement(any(), eq("crypto_field_locator"), any(), eq(ElementChecks.FOR_WRITING)))
                    .thenReturn(webElement);

            when(uiUtil.resolveSendKeysType(eq("encryptedValue123"), eq(webElement), eq(scenarioFile)))
                    .thenReturn("encryptedValue123");
        }
    }
}