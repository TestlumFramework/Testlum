package com.knubisoft.testlum.testing.framework.autohealing;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealedLocators;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlGeneratorTest {

    @Test
    void generatesPatchWithAllHealedValues() {
        HealedLocators healedLocators = new HealedLocators(
                List.of("//div[@id='a']", "//button"),
                List.of("div.btn", "input[name=\"q\"]"),
                "submit-id", "btn primary", "Save & Close");

        assertEquals("<healedLocator>\n"
                + "  <id>submit-id</id>\n"
                + "  <className>btn primary</className>\n"
                + "  <text>Save &amp; Close</text>\n"
                + "  <xpath>//div[@id='a']</xpath>\n"
                + "  <xpath>//button</xpath>\n"
                + "  <cssSelector>div.btn</cssSelector>\n"
                + "  <cssSelector>input[name=\"q\"]</cssSelector>\n"
                + "</healedLocator>\n", XmlGenerator.toXml(healedLocators));
    }

    @Test
    void skipsEmptyAndBlankValues() {
        HealedLocators healedLocators = new HealedLocators(
                List.of("//a"), List.of(), "  ", null, "text");

        assertEquals("<healedLocator>\n"
                + "  <text>text</text>\n"
                + "  <xpath>//a</xpath>\n"
                + "</healedLocator>\n", XmlGenerator.toXml(healedLocators));
    }

    @Test
    void generatesEmptyRootWhenNothingWasHealed() {
        assertEquals("<healedLocator/>\n", XmlGenerator.toXml(new HealedLocators()));
    }
}
