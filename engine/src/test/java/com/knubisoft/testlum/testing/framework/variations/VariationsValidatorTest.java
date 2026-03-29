package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariationsValidatorTest {

    @Mock
    private JacksonService jacksonService;

    @Mock
    private Scenario scenario;

    @Mock
    private Settings settings;

    @InjectMocks
    private VariationsValidator variationsValidator;

    @Test
    void emptyVariationListThrowsException() {
        File file = new File("/test/variations.csv");
        List<Map<String, String>> emptyList = new ArrayList<>();
        when(scenario.getSettings()).thenReturn(settings);
        when(settings.getVariations()).thenReturn("variations.csv");

        assertThrows(DefaultFrameworkException.class,
                () -> variationsValidator.validateByScenario(emptyList, scenario, file));
    }

    @Test
    void variationsNotUsedInScenarioThrowsException() {
        File file = new File("/test/variations.csv");
        Map<String, String> variationMap = new LinkedHashMap<>();
        variationMap.put("unusedVar", "value1");
        List<Map<String, String>> variationList = List.of(variationMap);

        when(jacksonService.writeValueAsString(scenario)).thenReturn("{\"commands\": []}");

        assertThrows(DefaultFrameworkException.class,
                () -> variationsValidator.validateByScenario(variationList, scenario, file));
    }

    @Test
    void validVariationsUsedInScenarioDoesNotThrow() {
        File file = new File("/test/variations.csv");
        Map<String, String> variationMap = new LinkedHashMap<>();
        variationMap.put("username", "testUser");
        List<Map<String, String>> variationList = List.of(variationMap);

        when(jacksonService.writeValueAsString(scenario))
                .thenReturn("{\"commands\": [{\"value\": \"{{username}}\"}]}");

        assertDoesNotThrow(
                () -> variationsValidator.validateByScenario(variationList, scenario, file));
    }

    @Test
    void multipleVariationKeysWhereAtLeastOneIsUsedDoesNotThrow() {
        Map<String, String> variationMap = new LinkedHashMap<>();
        variationMap.put("username", "testUser");
        variationMap.put("unusedKey", "unusedValue");
        List<Map<String, String>> variationList = List.of(variationMap);

        when(jacksonService.writeValueAsString(scenario))
                .thenReturn("{\"commands\": [{\"value\": \"{{username}}\"}]}");

        File file = new File("/test/variations.csv");
        assertDoesNotThrow(
                () -> variationsValidator.validateByScenario(variationList, scenario, file));
    }
}
