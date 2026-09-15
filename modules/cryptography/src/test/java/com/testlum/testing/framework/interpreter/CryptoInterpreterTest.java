package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.cryptography.CryptographyService;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.CryptoMethods;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CryptoInterpreterTest {

    private static final String TEST_ALIAS = "alias_btc";
    private static final String TEST_ENV = "test";
    private static final String TEST_SECRET = "secretKey123";
    private static final String TEST_METHOD = "AES";
    private static final String EXPECTED_ERROR_MSG = "Value must not be empty.";

    private CryptoInterpreter interpreter;
    private JacksonService jacksonService;
    private ScenarioContext scenarioContext;
    private CryptographyService cryptographyService;

    private Map<AliasEnv, Cryptography> cryptographyIntegrations;

    @BeforeEach
    void setUp() {
        final ApplicationContext context = mock(ApplicationContext.class);
        jacksonService = mock(JacksonService.class);
        cryptographyService = mock(CryptographyService.class);

        mockApplicationContext(context);
        scenarioContext = new ScenarioContext(new HashMap<>());

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File("test.xml"))
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment(TEST_ENV)
                .build();

        interpreter = new CryptoInterpreter(dependencies);

        cryptographyIntegrations = new HashMap<>();
        setField(interpreter, "cryptographyIntegrations", cryptographyIntegrations);
    }

    @Test
    @DisplayName("Should validate, trim, process via CryptographyService and write result into ScenarioContext")
    void shouldProcessCryptoAndSetScenarioContext() {
        final String rawValue = "  MY_SECRET_DATA  ";
        final String trimmedValue = "MY_SECRET_DATA";
        final String expectedEncryptedValue = "ENCRYPTED_BASE64_RESULT";

        final Crypto crypto = createCrypto("myVar", rawValue, TEST_ALIAS, "ENCRYPT");
        mockJackson(crypto);
        addCryptoIntegrationToMap(TEST_ALIAS, TEST_ENV, TEST_METHOD, TEST_SECRET);

        when(cryptographyService.processCommand(trimmedValue, "ENCRYPT", TEST_METHOD, TEST_SECRET, TEST_ALIAS))
                .thenReturn(expectedEncryptedValue);

        final CommandResult result = new CommandResult();
        result.setId(1);

        interpreter.apply(crypto, result);

        assertEquals(expectedEncryptedValue, scenarioContext.get("myVar"));
        verify(cryptographyService).processCommand(trimmedValue, "ENCRYPT", TEST_METHOD, TEST_SECRET, TEST_ALIAS);
    }

    @Test
    @DisplayName("Should throw DefaultFrameworkException when alias is not found in cryptographyIntegrations map")
    void shouldThrowExceptionWhenAliasNotConfigured() {
        final Crypto crypto = createCrypto("myVar", "DATA", "UNKNOWN_ALIAS", "ENCRYPT");
        mockJackson(crypto);

        final CommandResult result = new CommandResult();
        result.setId(1);

        final DefaultFrameworkException exception = assertThrows(
                DefaultFrameworkException.class,
                () -> interpreter.apply(crypto, result)
        );

        assertEquals(
                String.format("Cryptography integration is not configured for alias <UNKNOWN_ALIAS> and env <%s>",
                        TEST_ENV),
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should rethrow DefaultFrameworkException when CryptographyService fails")
    void shouldThrowExceptionWhenCryptographyServiceFails() {
        final String rawValue = "DATA_TO_DECRYPT";
        final Crypto crypto = createCrypto("myVar", rawValue, TEST_ALIAS, "DECRYPT");

        mockJackson(crypto);
        addCryptoIntegrationToMap(TEST_ALIAS, TEST_ENV, TEST_METHOD, TEST_SECRET);

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

    private void setField(final Object target, final String fieldName, final Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value via reflection", e);
        }
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
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
    }

    private void mockJackson(final Crypto crypto) {
        when(jacksonService.deepCopy(any(Crypto.class), eq(Crypto.class))).thenReturn(crypto);
        when(jacksonService.writeValueToCopiedString(any()))
                .thenReturn("{\"name\":\"" + crypto.getName() + "\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Crypto.class))).thenReturn(crypto);
    }

    private void addCryptoIntegrationToMap(final String alias, final String env,
                                           final String method, final String secret) {
        final Cryptography cryptoConfig = new Cryptography();
        cryptoConfig.setSecret(secret);
        cryptoConfig.setMethod(CryptoMethods.valueOf(method));

        cryptographyIntegrations.put(new AliasEnv(alias, env), cryptoConfig);
    }

    private Crypto createCrypto(final String name, final String value, final String alias, final String action) {
        final Crypto crypto = new Crypto();
        crypto.setName(name);
        crypto.setValue(value);
        crypto.setAlias(alias);
        crypto.setAction(action);
        return crypto;
    }
}