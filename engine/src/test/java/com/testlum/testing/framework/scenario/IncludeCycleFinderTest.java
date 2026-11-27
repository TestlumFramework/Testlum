package com.testlum.testing.framework.scenario;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.xml.XMLParser;
import com.testlum.testing.framework.xml.XMLParsers;
import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Include;
import com.testlum.testing.model.scenario.Repeat;
import com.testlum.testing.model.scenario.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncludeCycleFinderTest {

    private static final File SCENARIOS = new File("/tests/scenarios");

    @Mock
    private XMLParsers xmlParsers;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private TestResourceSettings testResourceSettings;
    @Mock
    private XMLParser<Scenario> scenarioParser;

    private IncludeCycleFinder finder;

    @BeforeEach
    void setUp() {
        lenient().when(testResourceSettings.getScenariosFolder()).thenReturn(SCENARIOS);
        lenient().when(xmlParsers.forScenario()).thenReturn(scenarioParser);
        finder = new IncludeCycleFinder(xmlParsers, fileSearcher, testResourceSettings);
    }

    @Test
    void returnsEmptyWhenThereIsNoCycle() {
        File a = scenario("a", include("b"));
        File b = scenario("b");

        assertTrue(finder.findCycle(a, b).isEmpty());
    }

    @Test
    void findsDirectCycle() {
        File a = scenario("a", include("b"));
        File b = scenario("b", include("a"));

        assertEquals("a -> b -> a", cycle(a, b));
    }

    @Test
    void findsIndirectCycle() {
        File a = scenario("a", include("b"));
        File b = scenario("b", include("c"));
        scenario("c", include("a"));

        assertEquals("a -> b -> c -> a", cycle(a, b));
    }

    @Test
    void findsCycleThatDoesNotContainTheScenario() {
        File x = scenario("x", include("a"));
        File a = scenario("a", include("b"));
        scenario("b", include("a"));

        assertEquals("a -> b -> a", cycle(x, a));
    }

    @Test
    void findsCycleThroughIncludeInsideRepeat() {
        Repeat repeat = new Repeat();
        repeat.getCommands().add(include("a"));
        File a = scenario("a", include("b"));
        File b = scenario("b", repeat);

        assertEquals("a -> b -> a", cycle(a, b));
    }

    @Test
    void skipsPathsWithVariables() {
        File a = scenario("a", include("b"));
        File b = scenario("b", include("{{next}}"));

        assertTrue(finder.findCycle(a, b).isEmpty());
    }

    @Test
    void skipsUnreadableScenario() {
        File a = scenario("a", include("b"));
        File b = scenario("b");
        when(scenarioParser.process(b)).thenThrow(new DefaultFrameworkException("broken"));

        assertTrue(finder.findCycle(a, b).isEmpty());
    }

    private File scenario(final String name, final AbstractCommand... commands) {
        File file = new File(SCENARIOS, name + "/scenario.xml");
        lenient().when(fileSearcher.searchFileFromDir(new File(SCENARIOS, name),
                TestResourceSettings.SCENARIO_FILENAME)).thenReturn(file);
        Scenario scenario = new Scenario();
        scenario.getCommands().addAll(List.of(commands));
        lenient().when(scenarioParser.process(file)).thenReturn(scenario);
        return file;
    }

    private Include include(final String path) {
        Include include = new Include();
        include.setScenario(path);
        return include;
    }

    private String cycle(final File scenarioFile, final File includedFile) {
        return finder.findCycle(scenarioFile, includedFile).map(finder::format).orElse(null);
    }
}
