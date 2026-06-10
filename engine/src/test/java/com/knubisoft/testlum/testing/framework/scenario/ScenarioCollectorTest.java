package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.IntegrationDisabledException;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariationsProvider;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.framework.xml.XMLParser;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Apis;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ScenarioCollector} verifying scenario collection,
 * XML parsing, command expansion (Auth/Include), and error handling.
 */
@ExtendWith(MockitoExtension.class)
class ScenarioCollectorTest {

    @Mock
    private ScenarioValidator scenarioValidator;
    @Mock
    private XMLParsers xmlParsers;
    @Mock
    private TestResourceSettings testResourceSettings;
    @Mock
    private IntegrationsUtil integrationUtil;
    @Mock
    private GlobalVariationsProvider globalVariationsProvider;
    @Mock
    private Integrations integrations;

    private ScenarioCollector collector;

    @BeforeEach
    void setUp() {
        collector = new ScenarioCollector(
                scenarioValidator, xmlParsers, testResourceSettings,
                integrationUtil, globalVariationsProvider, integrations);
    }

    @Nested
    class ResultStructure {

        @Test
        void emptyResultCreation() {
            final ScenarioCollector.Result result = new ScenarioCollector.Result();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void addMappingResultToResult() {
            final ScenarioCollector.Result result = new ScenarioCollector.Result();
            final File file = new File("scenario.xml");
            result.add(new ScenarioCollector.MappingResult(file, null, null));

            assertEquals(1, result.size());
            assertEquals(file, result.get(0).file);
        }

        @Test
        void multipleResultsCanBeAdded() {
            final ScenarioCollector.Result result = new ScenarioCollector.Result();
            result.add(new ScenarioCollector.MappingResult(new File("a.xml"), null, null));
            result.add(new ScenarioCollector.MappingResult(new File("b.xml"), null, null));
            result.add(new ScenarioCollector.MappingResult(new File("c.xml"), null, null));

            assertEquals(3, result.size());
        }
    }

    @Nested
    class MappingResultFields {

        @Test
        void mappingResultHoldsFileAndException() {
            final File file = new File("test.xml");
            final Exception exception = new RuntimeException("error");
            final ScenarioCollector.MappingResult mappingResult =
                    new ScenarioCollector.MappingResult(file, null, exception);

            assertEquals(file, mappingResult.file);
            assertNull(mappingResult.scenario);
            assertNotNull(mappingResult.exception);
            assertEquals("error", mappingResult.exception.getMessage());
        }

        @Test
        void mappingResultWithNullException() {
            final File file = new File("valid.xml");
            final ScenarioCollector.MappingResult mappingResult =
                    new ScenarioCollector.MappingResult(file, null, null);

            assertEquals(file, mappingResult.file);
            assertNull(mappingResult.exception);
        }

        @Test
        void mappingResultWithScenario() {
            final File file = new File("scenario.xml");
            final Scenario scenario = new Scenario();
            final ScenarioCollector.MappingResult result =
                    new ScenarioCollector.MappingResult(file, scenario, null);

            assertSame(scenario, result.scenario);
            assertNull(result.exception);
        }
    }

    @Nested
    class CollectScenarios {

        @TempDir
        Path tempDir;

        @Test
        void collectWithNoScopeUsesTestResourcesFolder(@TempDir final Path emptyDir) {
            when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.empty());
            when(testResourceSettings.getTestResourcesFolder()).thenReturn(emptyDir.toFile());

            ScenarioCollector.Result result = collector.collect();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void collectWithScopeFolderUsesScopeFolder(@TempDir final Path scopeDir) {
            when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.of(scopeDir.toFile()));

            ScenarioCollector.Result result = collector.collect();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @SuppressWarnings("unchecked")
        @Test
        void collectFindsScenarioXmlFiles() throws IOException {
            Path scenarioDir = tempDir.resolve("myScenario");
            Files.createDirectories(scenarioDir);
            Files.writeString(scenarioDir.resolve("scenario.xml"), "<scenario/>");

            when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.empty());
            when(testResourceSettings.getTestResourcesFolder()).thenReturn(tempDir.toFile());

            XMLParser<Scenario> parser = mock(XMLParser.class);
            when(xmlParsers.forScenario()).thenReturn(parser);

            Scenario scenario = new Scenario();
            Settings settings = new Settings();
            scenario.setSettings(settings);
            when(parser.process(any(File.class))).thenReturn(scenario);

            ScenarioCollector.Result result = collector.collect();

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @SuppressWarnings("unchecked")
        @Test
        void collectSkipsNonScenarioFiles() throws IOException {
            Path scenarioDir = tempDir.resolve("myScenario");
            Files.createDirectories(scenarioDir);
            Files.writeString(scenarioDir.resolve("other.xml"), "<other/>");

            when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.empty());
            when(testResourceSettings.getTestResourcesFolder()).thenReturn(tempDir.toFile());

            ScenarioCollector.Result result = collector.collect();

            assertTrue(result.isEmpty());
        }

        @SuppressWarnings("unchecked")
        @Test
        void collectHandlesIntegrationDisabledException() throws IOException {
            Path scenarioDir = tempDir.resolve("disabled");
            Files.createDirectories(scenarioDir);
            Files.writeString(scenarioDir.resolve("scenario.xml"), "<scenario/>");

            when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.empty());
            when(testResourceSettings.getTestResourcesFolder()).thenReturn(tempDir.toFile());

            XMLParser<Scenario> parser = mock(XMLParser.class);
            when(xmlParsers.forScenario()).thenReturn(parser);

            Scenario scenario = new Scenario();
            Settings settings = new Settings();
            scenario.setSettings(settings);
            when(parser.process(any(File.class))).thenReturn(scenario);
            doThrow(new IntegrationDisabledException("disabled"))
                    .when(scenarioValidator).validate(any(), any());

            ScenarioCollector.Result result = collector.collect();

            assertEquals(1, result.size());
            assertNotNull(result.get(0).scenario);
            assertNotNull(result.get(0).exception);
            assertInstanceOf(IntegrationDisabledException.class, result.get(0).exception);
        }

        @SuppressWarnings("unchecked")
        @Test
        void collectHandlesGenericException() throws IOException {
            Path scenarioDir = tempDir.resolve("broken");
            Files.createDirectories(scenarioDir);
            Files.writeString(scenarioDir.resolve("scenario.xml"), "<bad/>");

            when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.empty());
            when(testResourceSettings.getTestResourcesFolder()).thenReturn(tempDir.toFile());

            XMLParser<Scenario> parser = mock(XMLParser.class);
            when(xmlParsers.forScenario()).thenReturn(parser);
            when(parser.process(any(File.class))).thenThrow(new RuntimeException("parse error"));

            ScenarioCollector.Result result = collector.collect();

            assertEquals(1, result.size());
            assertNull(result.get(0).scenario);
            assertNotNull(result.get(0).exception);
        }

        @SuppressWarnings("unchecked")
        @Test
        void collectRecursivelyWalksSubdirectories() throws IOException {
            Path sub1 = tempDir.resolve("a/b");
            Path sub2 = tempDir.resolve("c");
            Files.createDirectories(sub1);
            Files.createDirectories(sub2);
            Files.writeString(sub1.resolve("scenario.xml"), "<s/>");
            Files.writeString(sub2.resolve("scenario.xml"), "<s/>");

            when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.empty());
            when(testResourceSettings.getTestResourcesFolder()).thenReturn(tempDir.toFile());

            XMLParser<Scenario> parser = mock(XMLParser.class);
            when(xmlParsers.forScenario()).thenReturn(parser);

            Scenario scenario = new Scenario();
            Settings settings = new Settings();
            scenario.setSettings(settings);
            when(parser.process(any(File.class))).thenReturn(scenario);

            ScenarioCollector.Result result = collector.collect();

            assertEquals(2, result.size());
        }
    }

    @Nested
    class CommandExpansion {

        @SuppressWarnings("unchecked")
        @Test
        void authCommandIsExpandedWithLogout() throws IOException, ReflectiveOperationException {
            Auth auth = new Auth();
            auth.setApiAlias("myApi");
            auth.setComment("login");
            auth.getCommands().add(mock(AbstractCommand.class));

            Api api = mock(Api.class);
            com.knubisoft.testlum.testing.model.global_config.Auth apiAuth =
                    mock(com.knubisoft.testlum.testing.model.global_config.Auth.class);
            when(api.getAuth()).thenReturn(apiAuth);
            when(apiAuth.isAutoLogout()).thenReturn(true);

            Apis apis = mock(Apis.class);
            when(integrations.getApis()).thenReturn(apis);
            when(apis.getApi()).thenReturn(List.of(api));
            when(integrationUtil.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            // Use reflection to test addAuthCommands
            Method addAuthCommands = ScenarioCollector.class.getDeclaredMethod(
                    "addAuthCommands", List.class, Auth.class);
            addAuthCommands.setAccessible(true);

            List<AbstractCommand> updatedCommands = new ArrayList<>();
            addAuthCommands.invoke(collector, updatedCommands, auth);

            // Auth + inner commands + Logout
            assertEquals(3, updatedCommands.size());
            assertInstanceOf(Auth.class, updatedCommands.get(0));
            assertInstanceOf(Logout.class, updatedCommands.get(2));
        }

        @SuppressWarnings("unchecked")
        @Test
        void authCommandWithNoAutoLogoutSkipsLogout() throws ReflectiveOperationException {
            Auth auth = new Auth();
            auth.setApiAlias("myApi");

            Api api = mock(Api.class);
            com.knubisoft.testlum.testing.model.global_config.Auth apiAuth =
                    mock(com.knubisoft.testlum.testing.model.global_config.Auth.class);
            when(api.getAuth()).thenReturn(apiAuth);
            when(apiAuth.isAutoLogout()).thenReturn(false);

            Apis apis = mock(Apis.class);
            when(integrations.getApis()).thenReturn(apis);
            when(apis.getApi()).thenReturn(List.of(api));
            when(integrationUtil.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Method addAuthCommands = ScenarioCollector.class.getDeclaredMethod(
                    "addAuthCommands", List.class, Auth.class);
            addAuthCommands.setAccessible(true);

            List<AbstractCommand> updatedCommands = new ArrayList<>();
            addAuthCommands.invoke(collector, updatedCommands, auth);

            // Auth only (no inner commands, no logout)
            assertEquals(1, updatedCommands.size());
            assertInstanceOf(Auth.class, updatedCommands.get(0));
        }

        @Test
        void isAutoLogoutThrowsWhenAuthNotConfigured() throws ReflectiveOperationException {
            Api api = mock(Api.class);
            when(api.getAuth()).thenReturn(null);
            when(api.getAlias()).thenReturn("myApi");

            Apis apis = mock(Apis.class);
            when(integrations.getApis()).thenReturn(apis);
            when(apis.getApi()).thenReturn(List.of(api));
            when(integrationUtil.findApiForAlias(any(), eq("myApi"))).thenReturn(api);

            Method isAutoLogout = ScenarioCollector.class.getDeclaredMethod("isAutoLogout", String.class);
            isAutoLogout.setAccessible(true);

            try {
                isAutoLogout.invoke(collector, "myApi");
                fail("Expected DefaultFrameworkException");
            } catch (java.lang.reflect.InvocationTargetException e) {
                assertInstanceOf(DefaultFrameworkException.class, e.getCause());
            }
        }

        @Test
        void regularCommandIsAddedAsIs() throws ReflectiveOperationException {
            AbstractCommand regularCmd = mock(AbstractCommand.class);

            Method addAbstractCommand = ScenarioCollector.class.getDeclaredMethod(
                    "addAbstractCommand", List.class, AbstractCommand.class, Optional.class);
            addAbstractCommand.setAccessible(true);

            List<AbstractCommand> result = new ArrayList<>();
            addAbstractCommand.invoke(collector, result, regularCmd, Optional.empty());

            assertEquals(1, result.size());
            assertSame(regularCmd, result.get(0));
        }
    }

    @Nested
    class VariationsHandling {

        @SuppressWarnings("unchecked")
        @Test
        void scenarioWithVariationsProcessesGlobalVariations() throws IOException {
            Path tempDir = Files.createTempDirectory("scenarioTest");
            try {
                Path scenarioDir = tempDir.resolve("test");
                Files.createDirectories(scenarioDir);
                Files.writeString(scenarioDir.resolve("scenario.xml"), "<s/>");

                when(testResourceSettings.getScenarioScopeFolder()).thenReturn(Optional.empty());
                when(testResourceSettings.getTestResourcesFolder()).thenReturn(tempDir.toFile());

                XMLParser<Scenario> parser = mock(XMLParser.class);
                when(xmlParsers.forScenario()).thenReturn(parser);

                Scenario scenario = new Scenario();
                Settings settings = new Settings();
                settings.setVariations("variations.csv");
                scenario.setSettings(settings);
                when(parser.process(any(File.class))).thenReturn(scenario);

                ScenarioCollector.Result result = collector.collect();

                assertEquals(1, result.size());
                verify(globalVariationsProvider).process(eq(scenario), any(File.class));
            } finally {
                // cleanup
                Files.walk(tempDir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }
}
