package com.testlum.testing.framework.util;

import com.testlum.testing.framework.autohealing.HealingScenarioUpdater;
import com.testlum.testing.framework.autohealing.dto.HealedLocators;
import com.testlum.testing.framework.autohealing.HealingScenarioUpdater.HealedLocator;
import com.testlum.testing.model.scenario.LocatorStrategy;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class HealingScenarioUpdaterTest {

    @TempDir
    Path tempDir;

    @Test
    void replacesOnlyTheMatchingAttributeAndKeepsFormatting() throws IOException {
        String scenario = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<scenario>\n"
                + "    <web comment=\"Start web browser\">\n"
                + "        <click comment=\"Click the sign in button\"\n"
                + "               locator=\"//div[@id='old']\" locatorStrategy=\"xpath\" highlight=\"true\"/>\n"
                + "    </web>\n"
                + "</scenario>\n";
        File file = write(scenario);

        int updated = update(file, "//div[@id='old']", LocatorStrategy.XPATH,
                new HealedLocator("//button[@name=\"save\"]", LocatorStrategy.XPATH));

        assertEquals(1, updated);
        assertEquals(scenario.replace("//div[@id='old']", "//button[@name=&quot;save&quot;]"), read(file));
    }

    @Test
    void updatesEveryOccurrenceWithTheSameValueAndStrategy() throws IOException {
        File file = write("<scenario>\n"
                + "    <click comment=\"Click it\" locator=\"//a\" locatorStrategy=\"xpath\"/>\n"
                + "    <assertPresent comment=\"It is there\" locator=\"//a\" locatorStrategy=\"xpath\"/>\n"
                + "</scenario>\n");

        int updated = update(file, "//a", LocatorStrategy.XPATH, new HealedLocator("//b", LocatorStrategy.XPATH));

        assertEquals(2, updated);
        assertEquals("<scenario>\n"
                + "    <click comment=\"Click it\" locator=\"//b\" locatorStrategy=\"xpath\"/>\n"
                + "    <assertPresent comment=\"It is there\" locator=\"//b\" locatorStrategy=\"xpath\"/>\n"
                + "</scenario>\n", read(file));
    }

    @Test
    void ignoresOtherStrategiesValuesAndCommentedOutCommands() throws IOException {
        String scenario = "<scenario>\n"
                + "    <click comment=\"Same value other strategy\" locator=\"//a\" locatorStrategy=\"cssSelector\"/>\n"
                + "    <click comment=\"Default strategy\" locator=\"//a\"/>\n"
                + "    <click comment=\"Other value\" locator=\"//c\" locatorStrategy=\"xpath\"/>\n"
                + "    <!-- <click comment=\"Disabled command\" locator=\"//a\" locatorStrategy=\"xpath\"/> -->\n"
                + "</scenario>\n";
        File file = write(scenario);

        int updated = update(file, "//a", LocatorStrategy.XPATH, new HealedLocator("//b", LocatorStrategy.XPATH));

        assertEquals(0, updated);
        assertEquals(scenario, read(file));
    }

    @Test
    void updatesToLocatorOfDragAndDrop() throws IOException {
        File file = write("<scenario>\n"
                + "    <dragAndDrop comment=\"Move the card\" toLocator=\"#old\" toLocatorStrategy=\"cssSelector\">\n"
                + "        <fromLocator>card.source</fromLocator>\n"
                + "    </dragAndDrop>\n"
                + "</scenario>\n");

        int updated = update(file, "#old", LocatorStrategy.CSS_SELECTOR,
                new HealedLocator("div.target", LocatorStrategy.CSS_SELECTOR));

        assertEquals(1, updated);
        assertEquals("<scenario>\n"
                + "    <dragAndDrop comment=\"Move the card\" toLocator=\"div.target\""
                + " toLocatorStrategy=\"cssSelector\">\n"
                + "        <fromLocator>card.source</fromLocator>\n"
                + "    </dragAndDrop>\n"
                + "</scenario>\n", read(file));
    }

    @Test
    void rewritesStrategyAttributeOnFallback() throws IOException {
        File file = write("<scenario>\n"
                + "    <input comment=\"Type the email\" locator=\"emailField\""
                + " locatorStrategy=\"id\" value=\"a@b.c\"/>\n"
                + "</scenario>\n");

        int updated = update(file, "emailField", LocatorStrategy.ID,
                new HealedLocator("//input[@name='email']", LocatorStrategy.XPATH));

        assertEquals(1, updated);
        assertEquals("<scenario>\n"
                + "    <input comment=\"Type the email\" locator=\"//input[@name='email']\""
                + " locatorStrategy=\"xpath\" value=\"a@b.c\"/>\n"
                + "</scenario>\n", read(file));
    }

    @Test
    void rewritesTextStrategyToXpathAndEscapesQuotes() throws IOException {
        File file = write("<scenario>\n"
                + "    <click comment=\"Click the link\" locator=\"Sign in\" locatorStrategy=\"text\"/>\n"
                + "</scenario>\n");

        int updated = update(file, "Sign in", LocatorStrategy.TEXT,
                new HealedLocator("//a[@role=\"link\"]", LocatorStrategy.XPATH));

        assertEquals(1, updated);
        assertEquals("<scenario>\n"
                + "    <click comment=\"Click the link\" locator=\"//a[@role=&quot;link&quot;]\""
                + " locatorStrategy=\"xpath\"/>\n"
                + "</scenario>\n", read(file));
    }

    @Test
    void matchesValueWrittenWithEntities() throws IOException {
        File file = write("<scenario>\n"
                + "    <click comment=\"Click save and close\" locator=\"//*[text()='Save &amp; Close']\""
                + " locatorStrategy=\"xpath\"/>\n"
                + "</scenario>\n");

        int updated = update(file, "//*[text()='Save & Close']", LocatorStrategy.XPATH,
                new HealedLocator("//*[text()='Save & Exit']", LocatorStrategy.XPATH));

        assertEquals(1, updated);
        assertEquals("<scenario>\n"
                + "    <click comment=\"Click save and close\" locator=\"//*[text()='Save &amp; Exit']\""
                + " locatorStrategy=\"xpath\"/>\n"
                + "</scenario>\n", read(file));
    }

    @Test
    void doesNotTouchTheFileWhenLocatorIsAVariable() throws IOException {
        String scenario = "<scenario>\n"
                + "    <click comment=\"Click the button\" locator=\"${signInButton}\" locatorStrategy=\"xpath\"/>\n"
                + "</scenario>\n";
        File file = write(scenario);

        int updated = update(file, "//button[@id='signIn']", LocatorStrategy.XPATH,
                new HealedLocator("//button", LocatorStrategy.XPATH));

        assertEquals(0, updated);
        assertEquals(scenario, read(file));
    }

    @Test
    void resolvesHealedValueByStrategy() {
        HealedLocators healedLocators = new HealedLocators(
                List.of("//first", "//second"), List.of("div.btn"), "submit", "btn", "Save");

        assertResolved("//first", LocatorStrategy.XPATH, healedLocators, LocatorStrategy.XPATH);
        assertResolved("div.btn", LocatorStrategy.CSS_SELECTOR, healedLocators, LocatorStrategy.CSS_SELECTOR);
        assertResolved("submit", LocatorStrategy.ID, healedLocators, LocatorStrategy.ID);
        assertResolved("btn", LocatorStrategy.CLASS, healedLocators, LocatorStrategy.CLASS);
        assertResolved("Save", LocatorStrategy.TEXT, healedLocators, LocatorStrategy.TEXT);
    }

    @Test
    void fallsBackToXpathWhenStrategyValueIsAbsent() {
        HealedLocators healedLocators = new HealedLocators(List.of("//first"), List.of(), null, null, null);

        assertResolved("//first", LocatorStrategy.XPATH, healedLocators, LocatorStrategy.ID);
    }

    @Test
    void resolvesNothingWhenNoXpathWasGenerated() {
        HealedLocators healedLocators = new HealedLocators(List.of(), List.of(), null, null, null);

        assertNull(HealingScenarioUpdater.resolveNewLocator(healedLocators, LocatorStrategy.ID));
    }

    private void assertResolved(final String expectedValue, final LocatorStrategy expectedStrategy,
                                final HealedLocators healedLocators, final LocatorStrategy strategy) {
        HealedLocator resolved = HealingScenarioUpdater.resolveNewLocator(healedLocators, strategy);
        assertEquals(expectedValue, resolved.getValue());
        assertSame(expectedStrategy, resolved.getStrategy());
    }

    private int update(final File file, final String oldValue, final LocatorStrategy oldStrategy,
                       final HealedLocator newLocator) {
        return HealingScenarioUpdater.updateInlineLocator(file, oldValue, oldStrategy, newLocator);
    }

    private File write(final String content) throws IOException {
        File file = tempDir.resolve("scenario.xml").toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private String read(final File file) throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }
}
