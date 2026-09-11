package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.exception.IncorrectCryptographyValueException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CryptoInterpreterTest {

    private static final String EXPECTED_ERROR_MSG =
            "The value must be a single, continuous string and must not contain any spaces.";

    private CryptoInterpreter interpreter;
    private JacksonService jacksonService;
    private ScenarioContext scenarioContext;

    @BeforeEach
    void setUp() {
        ApplicationContext context = mock(ApplicationContext.class);
        jacksonService = mock(JacksonService.class);
        ConditionProvider conditionProvider = mock(ConditionProvider.class);
        GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);

        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

        scenarioContext = new ScenarioContext(new HashMap<>());

        InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File("test.xml"))
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment("test")
                .build();

        interpreter = new CryptoInterpreter(dependencies);
    }

    @Test
    @DisplayName("Should successfully validate, "
            + "trim all starting and ending spaces and write value into ScenarioContext")
    void shouldValidateTrimAndSetScenarioContext() {
        Crypto copiedCrypto = createCrypto("myVar", "  SECRET_KEY_123  ", "alias_btc", "ENCRYPT");
        Crypto injectedCrypto = createCrypto("myVar", "  SECRET_KEY_123  ", "alias_btc", "ENCRYPT");

        when(jacksonService.deepCopy(any(Crypto.class), eq(Crypto.class))).thenReturn(copiedCrypto);
        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"name\":\"myVar\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Crypto.class))).thenReturn(injectedCrypto);

        CommandResult result = new CommandResult();
        result.setId(1);

        Crypto rawCrypto = createCrypto("myVar", "  SECRET_KEY_123  ", "alias_btc", "ENCRYPT");
        interpreter.apply(rawCrypto, result);

        assertEquals("SECRET_KEY_123", scenarioContext.get("myVar"));
    }

    @ParameterizedTest
    @DisplayName("Should throw IncorrectCryptographyValueException, if value contains spaces inside whole value")
    @ValueSource(strings = {
            "SECRET KEY",
            "SECRET\tKEY",
            "SECRET\nKEY",
            " SECRET KEY ",
            "a b"
    })
    void shouldThrowExceptionWhenValueContainsInternalSpaces(final String invalidValue) {
        Crypto injectedCrypto = createCrypto("myVar", invalidValue, "alias_btc", "ENCRYPT");

        when(jacksonService.deepCopy(any(Crypto.class), eq(Crypto.class))).thenReturn(injectedCrypto);
        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
        when(jacksonService.readCopiedValue(anyString(), eq(Crypto.class))).thenReturn(injectedCrypto);

        CommandResult result = new CommandResult();
        result.setId(1);

        Crypto rawCrypto = createCrypto("myVar", invalidValue, "alias_btc", "ENCRYPT");

        IncorrectCryptographyValueException exception = assertThrows(
                IncorrectCryptographyValueException.class,
                () -> interpreter.apply(rawCrypto, result)
        );

        assertEquals(EXPECTED_ERROR_MSG, exception.getMessage());

        assertThrows(
                IllegalArgumentException.class,
                () -> scenarioContext.get("myVar")
        );
    }

    @ParameterizedTest
    @DisplayName("Should throw exception, if value is empty or contains only spaces")
    @ValueSource(strings = {"", "   ", "\t\n"})
    void shouldThrowExceptionWhenValueIsEmptyOrOnlySpaces(final String emptyValue) {
        Crypto rawCrypto = createCrypto("myVar", emptyValue, "alias_btc", "ENCRYPT");
        Crypto injectedCrypto = createCrypto("myVar", emptyValue, "alias_btc", "ENCRYPT");

        when(jacksonService.deepCopy(any(Crypto.class), eq(Crypto.class))).thenReturn(injectedCrypto);
        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
        when(jacksonService.readCopiedValue(anyString(), eq(Crypto.class))).thenReturn(injectedCrypto);

        CommandResult result = new CommandResult();

        IncorrectCryptographyValueException exception = assertThrows(
                IncorrectCryptographyValueException.class,
                () -> interpreter.apply(rawCrypto, result)
        );

        assertEquals(EXPECTED_ERROR_MSG, exception.getMessage());
    }

    private Crypto createCrypto(final String name, final String value, final String alias, final String action) {
        Crypto crypto = new Crypto();
        crypto.setName(name);
        crypto.setValue(value);
        crypto.setAlias(alias);
        crypto.setAction(action);
        return crypto;
    }
}