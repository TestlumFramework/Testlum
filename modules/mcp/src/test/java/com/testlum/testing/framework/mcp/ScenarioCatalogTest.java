package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioCatalogTest {

    private static final String XML = "<scenario xmlns=\"http://www.testlum.com/testing/model/scenario\">"
            + "<overview><description>Reads one</description></overview>"
            + "<settings>%s<tags>%s</tags></settings>"
            + "<http alias=\"DEFAULT\"><get endpoint=\"/api/x/{{id}}\"><response code=\"200\"/></get></http>%s"
            + "</scenario>";

    @TempDir
    Path tmp;

    private final ScenarioCatalog catalog = new ScenarioCatalog();

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(tmp.resolve("data"));
        Files.writeString(tmp.resolve("data/ids.csv"), "id\n1\n2\n");
    }

    private void scenario(final String name, final String variations, final String tags, final String extra)
            throws IOException {
        Path d = tmp.resolve("scenarios").resolve(name);
        Files.createDirectories(d);
        String var = variations.isEmpty() ? "" : "<variations>" + variations + "</variations>";
        Files.writeString(d.resolve("scenario.xml"), XML.formatted(var, tags, extra));
    }

    private static String include(final String name) {
        return "<include scenario=\"" + name + "\"/>";
    }

    private String delete(final String name) {
        return catalog.delete(tmp, name);
    }

    @Test
    void listsEveryScenarioWithItsCallsAndIncludes() throws IOException {
        scenario("one", "ids.csv", "smoke,api", "");
        scenario("two", "", "api", include("one"));
        scenario("group/three", "", "api", "");
        String list = catalog.list(tmp);
        assertTrue(list.startsWith("3 scenarios\n"), list);
        assertTrue(list.contains("one | tags: smoke,api | variations: ids.csv\n  Reads one\n"
                + "  GET /api/x/{{id}}"), list);
        assertTrue(list.contains("  include one"), list);
        assertTrue(list.contains("group/three | tags: api\n"), list);
    }

    @Test
    void marksAScenarioThatIsSwitchedOff() throws IOException {
        Path d = Files.createDirectories(tmp.resolve("scenarios/off"));
        Files.writeString(d.resolve("scenario.xml"), XML.formatted("", "api", "").replace("<settings>",
                "<settings active=\"false\">"));
        assertTrue(catalog.list(tmp).contains("off | tags: api | INACTIVE"), catalog.list(tmp));
    }

    @Test
    void refusesToDeleteWhatIsIncluded() throws IOException {
        scenario("one", "", "api", "");
        scenario("two", "", "api", include("one"));
        assertTrue(assertThrows(ToolException.class, () -> delete("one")).getMessage().contains("two includes one"));
    }

    @Test
    void matchesIncludesOnTheFullPathNotTheLastName() throws IOException {
        scenario("a/login", "", "include", "");
        scenario("b/login", "", "include", "");
        scenario("user", "", "api", include("a/login"));
        assertThrows(ToolException.class, () -> delete("a/login"));
        assertTrue(delete("b/login").startsWith("Deleted scenarios/b/login"));
        assertTrue(Files.isDirectory(tmp.resolve("scenarios/a/login")));
    }

    @Test
    void anIncludeWithAVariableCountsAsIncludingAnyMatchingScenario() throws IOException {
        scenario("step/one", "", "include", "");
        scenario("user", "", "api", include("step/{{n}}"));
        assertThrows(ToolException.class, () -> delete("step/one"));
    }

    @Test
    void deletingAParentChecksItsDescendantsToo() throws IOException {
        scenario("group", "", "api", "");
        scenario("group/child", "", "include", "");
        scenario("user", "", "api", include("group/child"));
        assertTrue(assertThrows(ToolException.class, () -> delete("group")).getMessage()
                .contains("user includes group/child"));
        assertTrue(Files.isDirectory(tmp.resolve("scenarios/group/child")));
    }

    @Test
    void scenariosInsideTheDeletedFolderMayIncludeEachOther() throws IOException {
        scenario("group", "", "api", include("group/child"));
        scenario("group/child", "", "include", "");
        assertTrue(delete("group").startsWith("Deleted scenarios/group"));
        assertTrue(Files.notExists(tmp.resolve("scenarios/group")));
    }

    @Test
    void refusesToDeleteWhileAScenarioCannotBeRead() throws IOException {
        scenario("one", "", "api", "");
        Path broken = Files.createDirectories(tmp.resolve("scenarios/broken"));
        Files.writeString(broken.resolve("scenario.xml"), "<scenario>");
        String msg = assertThrows(ToolException.class, () -> delete("one")).getMessage();
        assertTrue(msg.contains("broken"), msg);
        assertTrue(Files.isDirectory(tmp.resolve("scenarios/one")));
    }

    @Test
    void neverDeletesDataAndNamesTheCsvNothingUsesAnyMore() throws IOException {
        scenario("one", "ids.csv", "api", "");
        scenario("group/three", "ids.csv", "api", "");
        assertEquals("Deleted scenarios/group/three", delete("group/three"));
        assertTrue(Files.exists(tmp.resolve("data/ids.csv")), "still used by one");
        assertEquals("Deleted scenarios/one. No scenario uses data/ids.csv now; it is left in place.", delete("one"));
        assertTrue(Files.exists(tmp.resolve("data/ids.csv")), "a data file is never deleted");
    }

    @Test
    void refusesBadNamesAndMissingScenarios() throws IOException {
        scenario("one", "", "api", "");
        assertThrows(ToolException.class, () -> delete("../data"));
        assertTrue(assertThrows(ToolException.class, () -> delete("ghost")).getMessage().contains("No such scenario"));
        assertTrue(Files.isDirectory(tmp.resolve("scenarios/one")));
    }

    @Test
    void aFolderNameThatOnlyStartsTheSameIsLeftAlone() throws IOException {
        scenario("group", "", "api", "");
        scenario("group-two", "", "api", "");
        scenario("user", "", "api", include("group-two"));
        assertTrue(delete("group").startsWith("Deleted scenarios/group"));
        assertTrue(Files.isDirectory(tmp.resolve("scenarios/group-two")));
    }

    @Test
    void aRepeatThatReadsACsvKeepsItOffTheUnusedList() throws IOException {
        scenario("one", "ids.csv", "api", "");
        scenario("two", "", "api", "<repeat variations=\"ids.csv\"/>");
        assertEquals("Deleted scenarios/one", delete("one"));
    }

    @Test
    void anIncludeIsResolvedAsAPathTheWayTheEngineDoes() throws IOException {
        scenario("group/child", "", "include", "");
        Files.createDirectories(tmp.resolve("scenarios/group/other"));
        scenario("dot", "", "api", include("group/./child"));
        assertThrows(ToolException.class, () -> delete("group/child"));
        Files.delete(tmp.resolve("scenarios/dot/scenario.xml"));
        scenario("up", "", "api", include("group/other/../child"));
        assertThrows(ToolException.class, () -> delete("group/child"));
    }

    @Test
    void anIncludeOfAMissingFolderRunsTheNearestScenarioAboveIt() throws IOException {
        scenario("group", "", "include", "");
        scenario("user", "", "api", include("group/nothing/deeper"));
        assertTrue(assertThrows(ToolException.class, () -> delete("group")).getMessage()
                .contains("user includes group"));
    }

    @Test
    void anIncludeThatPointsAtOtherScenariosDoesNotBlock() throws IOException {
        scenario("one", "", "include", "");
        scenario("two", "", "include", "");
        scenario("user", "", "api", include("two"));
        assertTrue(delete("one").startsWith("Deleted scenarios/one"));
    }

    @Test
    void anIncludeWithAVariableAndARelativePartBlocksEveryDelete() throws IOException {
        scenario("one", "", "include", "");
        scenario("user", "", "api", include("{{part}}/../one"));
        assertTrue(assertThrows(ToolException.class, () -> delete("one")).getMessage()
                .contains("could be any scenario"));
    }

    @Test
    void aVariableIncludeAlsoCoversAScenarioBelowTheMatch() throws IOException {
        scenario("step/one/sub", "", "include", "");
        scenario("user", "", "api", include("step/{{n}}"));
        assertThrows(ToolException.class, () -> delete("step/one/sub"));
    }

    @Test
    void aVariableStandsForAnythingIncludingSeveralSegments() throws IOException {
        scenario("group", "", "include", "");
        scenario("group/a/leaf", "", "include", "");
        scenario("user", "", "api", include("group/{{part}}/leaf"));
        assertThrows(ToolException.class, () -> delete("group/a/leaf"));
        assertThrows(ToolException.class, () -> delete("group"));
    }

    @Test
    void aLeadingDotSlashDoesNotHideAVariableInclude() throws IOException {
        scenario("group/x", "", "include", "");
        scenario("user", "", "api", include("./group/{{part}}"));
        assertThrows(ToolException.class, () -> delete("group/x"));
    }

    @Test
    void backslashesAndLetterCaseDoNotHideAnInclude() throws IOException {
        scenario("group/child", "", "include", "");
        scenario("user", "", "api", include("group\\other\\..\\child"));
        assertThrows(ToolException.class, () -> delete("group/child"));
        Files.delete(tmp.resolve("scenarios/user/scenario.xml"));
        scenario("shout", "", "api", include("GROUP/Child"));
        assertThrows(ToolException.class, () -> delete("group/child"));
    }

    @Test
    void doesNotNameACsvThatWasNeverThere() throws IOException {
        scenario("one", "ghost.csv", "api", "");
        assertEquals("Deleted scenarios/one", delete("one"));
    }

    @Test
    void ignoresAVariationsNameThatPointsOutsideTheDataFolder() throws IOException {
        scenario("one", "../outside.csv", "api", "");
        Files.writeString(tmp.resolve("outside.csv"), "id\n");
        assertEquals("Deleted scenarios/one", delete("one"));
        assertTrue(Files.exists(tmp.resolve("outside.csv")));
    }
}
