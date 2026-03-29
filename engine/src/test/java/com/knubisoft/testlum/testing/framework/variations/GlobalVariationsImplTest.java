package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalVariationsImplTest {

    @Mock
    private GlobalVariationsProvider globalVariationsProvider;

    @Mock
    private ScenarioContext scenarioContext;

    @InjectMocks
    private GlobalVariationsImpl globalVariationsImpl;

    @Test
    void getVariationsDelegatesToProvider() {
        String fileName = "variations.csv";
        List<Map<String, String>> expected = List.of(Map.of("key", "value"));
        when(globalVariationsProvider.getVariations(fileName)).thenReturn(expected);

        List<Map<String, String>> result = globalVariationsImpl.getVariations(fileName);

        assertEquals(expected, result);
        verify(globalVariationsProvider).getVariations(fileName);
    }

    @Test
    void getValueWithTwoArgsDelegatesToProvider() {
        String variation = "{{variable}}";
        Map<String, String> variationMap = Map.of("variable", "resolvedValue");
        when(globalVariationsProvider.getValue(variation, variationMap)).thenReturn("resolvedValue");

        String result = globalVariationsImpl.getValue(variation, variationMap);

        assertEquals("resolvedValue", result);
        verify(globalVariationsProvider).getValue(variation, variationMap);
    }

    @Test
    void getValueWithThreeArgsDelegatesToProvider() {
        String variation = "{{variable}}";
        Map<String, String> variationMap = Map.of("variable", "resolvedValue");
        when(globalVariationsProvider.getValue(variation, variationMap, scenarioContext))
                .thenReturn("contextResolved");

        String result = globalVariationsImpl.getValue(variation, variationMap, scenarioContext);

        assertEquals("contextResolved", result);
        verify(globalVariationsProvider).getValue(variation, variationMap, scenarioContext);
    }
}
