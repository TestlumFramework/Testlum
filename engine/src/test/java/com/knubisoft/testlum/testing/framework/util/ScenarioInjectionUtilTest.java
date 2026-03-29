package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioInjectionUtilTest {

    @Mock
    private GlobalVariationsProvider globalVariationsProvider;

    @Mock
    private InjectionService injectionService;

    @Mock
    private ScenarioContext scenarioContext;

    @InjectMocks
    private ScenarioInjectionUtil scenarioInjectionUtil;

    @Test
    void injectObjectDelegatesToInjectionServiceWithScenarioContext() {
        String input = "test-input";
        String expected = "injected-result";
        when(injectionService.inject(eq(input), any(Function.class))).thenReturn(expected);

        String result = scenarioInjectionUtil.injectObject(input, scenarioContext);

        assertEquals(expected, result);
        verify(injectionService).inject(eq(input), any(Function.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void injectObjectUsesScenarioContextInjectAsOperator() {
        String input = "{{variable}}";
        when(scenarioContext.inject("variable")).thenReturn("resolved");
        ArgumentCaptor<Function<String, String>> captor = ArgumentCaptor.forClass(Function.class);
        when(injectionService.inject(eq(input), captor.capture())).thenReturn(input);

        scenarioInjectionUtil.injectObject(input, scenarioContext);

        Function<String, String> operator = captor.getValue();
        String result = operator.apply("variable");
        assertEquals("resolved", result);
        verify(scenarioContext).inject("variable");
    }

    @Test
    void injectObjectVariationWithTwoArgsDelegatesToGlobalVariationsProvider() {
        String input = "test-input";
        String expected = "variation-result";
        Map<String, String> variation = Map.of("key", "value");
        when(injectionService.inject(eq(input), any(Function.class))).thenReturn(expected);

        String result = scenarioInjectionUtil.injectObjectVariation(input, variation);

        assertEquals(expected, result);
        verify(injectionService).inject(eq(input), any(Function.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void injectObjectVariationWithTwoArgsUsesGetValue() {
        String input = "test";
        Map<String, String> variation = Map.of("key", "value");
        when(globalVariationsProvider.getValue("key", variation)).thenReturn("resolved-value");
        ArgumentCaptor<Function<String, String>> captor = ArgumentCaptor.forClass(Function.class);
        when(injectionService.inject(eq(input), captor.capture())).thenReturn(input);

        scenarioInjectionUtil.injectObjectVariation(input, variation);

        Function<String, String> operator = captor.getValue();
        String result = operator.apply("key");
        assertEquals("resolved-value", result);
        verify(globalVariationsProvider).getValue("key", variation);
    }

    @Test
    void injectObjectVariationWithThreeArgsDelegatesToGlobalVariationsProvider() {
        String input = "test-input";
        String expected = "variation-ctx-result";
        Map<String, String> variation = Map.of("key", "value");
        when(injectionService.inject(eq(input), any(Function.class))).thenReturn(expected);

        String result = scenarioInjectionUtil.injectObjectVariation(input, variation, scenarioContext);

        assertEquals(expected, result);
        verify(injectionService).inject(eq(input), any(Function.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void injectObjectVariationWithThreeArgsUsesGetValueWithContext() {
        String input = "test";
        Map<String, String> variation = Map.of("key", "value");
        when(globalVariationsProvider.getValue("key", variation, scenarioContext)).thenReturn("ctx-resolved");
        ArgumentCaptor<Function<String, String>> captor = ArgumentCaptor.forClass(Function.class);
        when(injectionService.inject(eq(input), captor.capture())).thenReturn(input);

        scenarioInjectionUtil.injectObjectVariation(input, variation, scenarioContext);

        Function<String, String> operator = captor.getValue();
        String result = operator.apply("key");
        assertEquals("ctx-resolved", result);
        verify(globalVariationsProvider).getValue("key", variation, scenarioContext);
    }
}
