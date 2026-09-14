package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.exception.IncorrectCryptographyValueException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.IntegrationsProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.CryptoMethods;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CryptoInterpreterTest {

    private static final String EXPECTED_ERROR_MSG =
            "Value must not be empty.";
    private static final String TEST_ALIAS = "alias_btc";
    private static final String TEST_SECRET = "secretKey123";
    private static final String TEST_METHOD = "AES";

    private CryptoInterpreter interpreter;
    private JacksonService jacksonService;
    private ScenarioContext scenarioContext;
    private CryptographyService cryptographyService;
    private IntegrationsProvider integrationsProvider;

    @BeforeEach
    void setUp() {
        final ApplicationContext context = mock(ApplicationContext.class);
        jacksonService = mock(JacksonService.class);
        cryptographyService = mock(CryptographyService.class);
        integrationsProvider = mock(IntegrationsProvider.class);

        mockApplicationContext(context);
        scenarioContext = new ScenarioContext(new HashMap<>());

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File("test.xml"))
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment("test")
                .build();

        interpreter = new CryptoInterpreter(dependencies);
    }

    @Test
    @DisplayName("Should validate, trim, process via CryptographyService and write result into ScenarioContext")
    void shouldProcessCryptoAndSetScenarioContext() {
        final String rawValue = "  MY_SECRET_DATA  ";
        final String trimmedValue = "MY_SECRET_DATA";
        final String expectedEncryptedValue = "ENCRYPTED_BASE64_RESULT";

        final Crypto crypto = createCrypto("myVar", rawValue, TEST_ALIAS, "ENCRYPT");
        mockJackson(crypto);
        mockCryptoIntegration();

        when(cryptographyService.processCommand(trimmedValue, "ENCRYPT", TEST_METHOD, TEST_SECRET, TEST_ALIAS))
                .thenReturn(expectedEncryptedValue);

        final CommandResult result = new CommandResult();
        result.setId(1);

        interpreter.apply(crypto, result);

        assertEquals(expectedEncryptedValue, scenarioContext.get("myVar"));
        verify(cryptographyService).processCommand(trimmedValue, "ENCRYPT", TEST_METHOD, TEST_SECRET, TEST_ALIAS);
    }

    @Test
    @DisplayName("Should rethrow DefaultFrameworkException when CryptographyService fails")
    void shouldThrowExceptionWhenCryptographyServiceFails() {
        final String rawValue = "DATA_TO_DECRYPT";
        final Crypto crypto = createCrypto("myVar", rawValue, TEST_ALIAS, "DECRYPT");

        mockJackson(crypto);
        mockCryptoIntegration();

        when(cryptographyService.processCommand(rawValue, "DECRYPT", TEST_METHOD, TEST_SECRET, TEST_ALIAS))
                .thenThrow(new DefaultFrameworkException("Decryption failed"));

        final CommandResult result = new CommandResult();
        result.setId(1);

        final DefaultFrameworkException exception = assertThrows(
                DefaultFrameworkException.class,
                () -> interpreter.apply(crypto, result)
        );

        assertEquals("Decryption failed", exception.getMessage());
    }

//    @ParameterizedTest
//    @DisplayName("Should throw IncorrectCryptographyValueException, if value contains spaces inside whole value")
//    @ValueSource(strings = {"SECRET KEY", "SECRET\tKEY", "SECRET\nKEY", " SECRET KEY ", "a b"})
//    void shouldThrowExceptionWhenValueContainsInternalSpaces(final String invalidValue) {
//        final Crypto injectedCrypto = createCrypto("myVar", invalidValue, TEST_ALIAS, "ENCRYPT");
//        mockJackson(injectedCrypto);
//
//        final CommandResult result = new CommandResult();
//        result.setId(1);
//
//        final Crypto rawCrypto = createCrypto("myVar", invalidValue, TEST_ALIAS, "ENCRYPT");
//
//        final IncorrectCryptographyValueException exception = assertThrows(
//                IncorrectCryptographyValueException.class,
//                () -> interpreter.apply(rawCrypto, result)
//        );
//
//        assertEquals(EXPECTED_ERROR_MSG, exception.getMessage());
//        assertThrows(IllegalArgumentException.class, () -> scenarioContext.get("myVar"));
//    }

    @ParameterizedTest
    @DisplayName("Should throw exception, if value is empty or contains only spaces")
    @ValueSource(strings = {"", "   ", "\t\n"})
    void shouldThrowExceptionWhenValueIsEmptyOrOnlySpaces(final String emptyValue) {
        final Crypto crypto = createCrypto("myVar", emptyValue, TEST_ALIAS, "ENCRYPT");
        mockJackson(crypto);

        final CommandResult result = new CommandResult();

        final IncorrectCryptographyValueException exception = assertThrows(
                IncorrectCryptographyValueException.class,
                () -> interpreter.apply(crypto, result)
        );

        assertEquals(EXPECTED_ERROR_MSG, exception.getMessage());
    }

    private void mockApplicationContext(final ApplicationContext context) {
        final ConditionProvider conditionProvider = mock(ConditionProvider.class);
        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);

        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        when(context.getBean(CryptographyService.class)).thenReturn(cryptographyService);
        when(context.getBean(IntegrationsProvider.class)).thenReturn(integrationsProvider);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
    }

    private void mockJackson(final Crypto crypto) {
        when(jacksonService.deepCopy(any(Crypto.class),
                eq(Crypto.class))).thenReturn(crypto);
        when(jacksonService.writeValueToCopiedString(any()))
                .thenReturn("{\"name\":\"" + crypto.getName() + "\"}");
        when(jacksonService.readCopiedValue(anyString(),
                eq(Crypto.class))).thenReturn(crypto);
    }

    private void mockCryptoIntegration() {
        final Cryptography cryptoConfig = new Cryptography();
        cryptoConfig.setSecret(TEST_SECRET);
        cryptoConfig.setMethod(CryptoMethods.valueOf(TEST_METHOD));

        when(integrationsProvider.findListByEnv(eq(Cryptography.class), anyString()))
                .thenReturn(Collections.singletonList(cryptoConfig));
        when(integrationsProvider.findCryptographyForAlias(any(), eq(TEST_ALIAS)))
                .thenReturn(cryptoConfig);
    }

    private Crypto createCrypto(final String name, final String value,
                                final String alias, final String action) {
        final Crypto crypto = new Crypto();
        crypto.setName(name);
        crypto.setValue(value);
        crypto.setAlias(alias);
        crypto.setAction(action);
        return crypto;
    }
}