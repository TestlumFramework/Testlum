package com.testlum.testing.framework.util;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.exception.FileLinkingException;
import com.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.testlum.testing.framework.xml.XMLParser;
import com.testlum.testing.framework.xml.XMLParsers;
import com.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ScenarioStepReaderTest {

    private static final String VARIATIONS_FILE = "variations.csv";

    private final File scenariosFolder = new File("scenarios");
    private final File rootFile = new File(scenariosFolder, "root/scenario.xml");
    private final Map<File, Scenario> parsedScenarios = new HashMap<>();

    private XMLParser<Scenario> scenarioParser;
    private FileSearcher fileSearcher;
    private GlobalVariationsProvider globalVariationsProvider;
    private ScenarioStepReader reader;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        scenarioParser = mock(XMLParser.class);
        XMLParsers xmlParsers = mock(XMLParsers.class);
        when(xmlParsers.forScenario()).thenReturn(scenarioParser);
        fileSearcher = mock(FileSearcher.class);
        TestResourceSettings testResourceSettings = mock(TestResourceSettings.class);
        when(testResourceSettings.getScenariosFolder()).thenReturn(scenariosFolder);
        globalVariationsProvider = mock(GlobalVariationsProvider.class);
        reader = new ScenarioStepReader(xmlParsers, fileSearcher, testResourceSettings, globalVariationsProvider);
    }

    private Scenario scenario(final AbstractCommand... commands) {
        Scenario scenario = mock(Scenario.class);
        when(scenario.getCommands()).thenReturn(Arrays.asList(commands));
        return scenario;
    }

    private Scenario scenarioWithVariations(final AbstractCommand... commands) {
        Scenario scenario = scenario(commands);
        Settings settings = mock(Settings.class);
        when(settings.getVariations()).thenReturn(VARIATIONS_FILE);
        when(scenario.getSettings()).thenReturn(settings);
        return scenario;
    }

    private Include include(final String path) {
        Include include = mock(Include.class);
        when(include.getScenario()).thenReturn(path);
        return include;
    }

    private Repeat repeat(final AbstractCommand... commands) {
        Repeat repeat = mock(Repeat.class);
        when(repeat.getCommands()).thenReturn(Arrays.asList(commands));
        return repeat;
    }

    private File linkInclude(final String path) {
        File includedFile = new File(new File(scenariosFolder, path), TestResourceSettings.SCENARIO_FILENAME);
        when(fileSearcher.searchFileFromDir(new File(scenariosFolder, path),
                TestResourceSettings.SCENARIO_FILENAME)).thenReturn(includedFile);
        return includedFile;
    }

    private File indexedInclude(final String path, final Scenario included) {
        File includedFile = linkInclude(path);
        parsedScenarios.put(includedFile, included);
        return includedFile;
    }

    private ScenarioStepReader.Result scan(final Scenario scenario) {
        return reader.scan(scenario, rootFile, parsedScenarios);
    }

    @SafeVarargs
    private void withVariations(final Map<String, String>... variations) {
        List<Map<String, String>> variationList = new ArrayList<>(Arrays.asList(variations));
        when(globalVariationsProvider.getVariations(VARIATIONS_FILE)).thenReturn(variationList);
    }

    @Nested
    class FlatCommands {

        @Test
        void shouldDetectWebCommand() {
            ScenarioStepReader.Result result = scan(scenario(mock(Web.class)));

            assertTrue(result.isWebPresent());
            assertFalse(result.isMobileBrowserPresent());
            assertFalse(result.isNativePresent());
        }

        @Test
        void shouldDetectMobileBrowserCommand() {
            ScenarioStepReader.Result result = scan(scenario(mock(Mobilebrowser.class)));

            assertFalse(result.isWebPresent());
            assertTrue(result.isMobileBrowserPresent());
            assertFalse(result.isNativePresent());
        }

        @Test
        void shouldDetectNativeCommand() {
            ScenarioStepReader.Result result = scan(scenario(mock(Native.class)));

            assertFalse(result.isWebPresent());
            assertFalse(result.isMobileBrowserPresent());
            assertTrue(result.isNativePresent());
        }

        @Test
        void shouldDetectAllCommandTypes() {
            ScenarioStepReader.Result result = scan(
                    scenario(mock(Web.class), mock(Mobilebrowser.class), mock(Native.class)));

            assertTrue(result.isWebPresent());
            assertTrue(result.isMobileBrowserPresent());
            assertTrue(result.isNativePresent());
        }

        @Test
        void shouldReturnAllFalseWhenNoCommands() {
            ScenarioStepReader.Result result = scan(scenario());

            assertFalse(result.isWebPresent());
            assertFalse(result.isMobileBrowserPresent());
            assertFalse(result.isNativePresent());
        }

        @Test
        void shouldReturnAllFalseForOtherCommandTypes() {
            ScenarioStepReader.Result result = scan(scenario(mock(AbstractCommand.class)));

            assertFalse(result.isWebPresent());
            assertFalse(result.isMobileBrowserPresent());
            assertFalse(result.isNativePresent());
        }
    }

    @Nested
    class NestedContainers {

        @Test
        void shouldDetectWebInsideRepeat() {
            assertTrue(scan(scenario(repeat(mock(Web.class)))).isWebPresent());
        }

        @Test
        void shouldDetectWebInsideNestedRepeat() {
            assertTrue(scan(scenario(repeat(repeat(mock(Web.class))))).isWebPresent());
        }

        @Test
        void shouldDetectWebInsideAuth() {
            Auth auth = mock(Auth.class);
            when(auth.getCommands()).thenReturn(Collections.singletonList(mock(Web.class)));

            assertTrue(scan(scenario(auth)).isWebPresent());
        }
    }

    @Nested
    class IncludedScenarios {

        @Test
        void shouldDetectWebInsideInclude() {
            indexedInclude("login", scenario(mock(Web.class)));

            assertTrue(scan(scenario(include("login"))).isWebPresent());
        }

        @Test
        void shouldDetectMobileBrowserInsideInclude() {
            indexedInclude("login", scenario(mock(Mobilebrowser.class)));

            assertTrue(scan(scenario(include("login"))).isMobileBrowserPresent());
        }

        @Test
        void shouldDetectNativeInsideInclude() {
            indexedInclude("login", scenario(mock(Native.class)));

            assertTrue(scan(scenario(include("login"))).isNativePresent());
        }

        @Test
        void shouldDetectWebInsideChainedIncludes() {
            indexedInclude("inner", scenario(mock(Web.class)));
            indexedInclude("outer", scenario(include("inner")));

            assertTrue(scan(scenario(include("outer"))).isWebPresent());
        }

        @Test
        void shouldDetectWebInsideIncludeNestedInRepeat() {
            indexedInclude("login", scenario(mock(Web.class)));

            assertTrue(scan(scenario(repeat(include("login")))).isWebPresent());
        }

        @Test
        void shouldStopOnIncludeCycle() {
            Scenario second = scenario(include("first"));
            Scenario first = scenario(include("second"), mock(Native.class));
            indexedInclude("first", first);
            indexedInclude("second", second);

            ScenarioStepReader.Result result = scan(scenario(include("first")));

            assertTrue(result.isNativePresent());
            assertFalse(result.isWebPresent());
        }

        @Test
        void shouldStopWhenIncludePointsToRootScenario() {
            File selfInclude = linkInclude("root");
            parsedScenarios.put(selfInclude, scenario(mock(Web.class)));

            assertFalse(scan(scenario(include("root"))).isWebPresent());
        }

        @Test
        void shouldParseIncludedScenarioWhenItIsNotIndexed() {
            File includedFile = linkInclude("login");
            Scenario included = scenario(mock(Web.class));
            when(scenarioParser.process(includedFile)).thenReturn(included);

            assertTrue(scan(scenario(include("login"))).isWebPresent());
            verify(scenarioParser, times(1)).process(includedFile);
        }

        @Test
        void shouldParseSameIncludedScenarioOnlyOnce() {
            File includedFile = linkInclude("login");
            Scenario included = scenario();
            when(scenarioParser.process(includedFile)).thenReturn(included);

            scan(scenario(include("login"), include("login")));

            verify(scenarioParser, times(1)).process(includedFile);
        }
    }

    @Nested
    class UnresolvableIncludes {

        @Test
        void shouldResolveIncludePathByEveryVariation() {
            Map<String, String> first = new LinkedHashMap<>();
            first.put("flow", "isWebPresent-flow");
            Map<String, String> second = new LinkedHashMap<>();
            second.put("flow", "native-flow");
            withVariations(first, second);
            when(globalVariationsProvider.getValue("{{flow}}", first)).thenReturn("isWebPresent-flow");
            when(globalVariationsProvider.getValue("{{flow}}", second)).thenReturn("native-flow");
            indexedInclude("isWebPresent-flow", scenario(mock(Web.class)));
            indexedInclude("native-flow", scenario(mock(Native.class)));

            ScenarioStepReader.Result result = scan(scenarioWithVariations(include("{{flow}}")));

            assertTrue(result.isWebPresent());
            assertTrue(result.isNativePresent());
        }

        @Test
        void shouldSkipIncludeWhenPathVariableIsUnknown() {
            Map<String, String> variation = new LinkedHashMap<>();
            variation.put("other", "value");
            withVariations(variation);
            when(globalVariationsProvider.getValue(eq("{{flow}}"), anyMap()))
                    .thenThrow(new IllegalArgumentException("no value"));

            ScenarioStepReader.Result result = scan(scenarioWithVariations(include("{{flow}}")));

            assertFalse(result.isWebPresent());
            assertFalse(result.isMobileBrowserPresent());
            assertFalse(result.isNativePresent());
        }

        @Test
        void shouldSkipIncludeWithVariableWhenScenarioHasNoVariations() {
            assertFalse(scan(scenario(include("{{flow}}"))).isWebPresent());
        }

        @Test
        void shouldSkipIncludeWhenVariationsCannotBeRead() {
            when(globalVariationsProvider.getVariations(VARIATIONS_FILE))
                    .thenThrow(new IllegalStateException("variations not found"));

            assertFalse(scan(scenarioWithVariations(include("{{flow}}"))).isWebPresent());
        }

        @Test
        void shouldSkipIncludeWhenFileIsNotFound() {
            when(fileSearcher.searchFileFromDir(any(File.class), anyString()))
                    .thenThrow(new FileLinkingException(scenariosFolder, scenariosFolder, "scenario.xml"));

            assertFalse(scan(scenario(include("login"))).isWebPresent());
        }

        @Test
        void shouldSkipIncludeWhenFileCannotBeParsed() {
            File includedFile = linkInclude("login");
            when(scenarioParser.process(includedFile)).thenThrow(new RuntimeException("broken xml"));

            assertFalse(scan(scenario(include("login"))).isWebPresent());
        }
    }
}
