package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for {@link XMLComparator} verifying node name, attribute, and child node comparison. */
class XMLComparatorTest {

    private final XMLComparator strict = new XMLComparator(Mode.STRICT);
    private final XMLComparator lenient = new XMLComparator(Mode.LENIENT);

    private Node parseXmlElement(final String xml) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)))
                    .getDocumentElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class BasicNodeComparison {
        @Test
        void identicalSimpleXmlDoesNotThrow() {
            assertDoesNotThrow(() -> lenient.compare(
                    parseXmlElement("<root>text</root>"),
                    parseXmlElement("<root>text</root>")));
        }

        @Test
        void differentNodeNameThrows() {
            assertThrows(MatchException.class, () -> lenient.compare(
                    parseXmlElement("<root/>"),
                    parseXmlElement("<other/>")));
        }

        @Test
        void emptyElementsInStrictMode() {
            assertDoesNotThrow(() -> strict.compare(
                    parseXmlElement("<root/>"),
                    parseXmlElement("<root/>")));
        }
    }

    @Nested
    class AttributeComparison {
        @Test
        void identicalAttributesInLenientMode() {
            assertDoesNotThrow(() -> lenient.compare(
                    parseXmlElement("<root attr=\"value\"/>"),
                    parseXmlElement("<root attr=\"value\"/>")));
        }

        @Test
        void differentAttributeValueThrows() {
            assertThrows(MatchException.class, () -> lenient.compare(
                    parseXmlElement("<root attr=\"v1\"/>"),
                    parseXmlElement("<root attr=\"v2\"/>")));
        }

        @Test
        void missingAttributeInActualThrows() {
            assertThrows(MatchException.class, () -> lenient.compare(
                    parseXmlElement("<root attr=\"value\"/>"),
                    parseXmlElement("<root/>")));
        }

        @Test
        void lenientModeExtraAttributeOk() {
            assertDoesNotThrow(() -> lenient.compare(
                    parseXmlElement("<root a=\"1\"/>"),
                    parseXmlElement("<root a=\"1\" b=\"2\"/>")));
        }

        @Test
        void strictModeExtraAttributeThrows() {
            assertThrows(MatchException.class, () -> strict.compare(
                    parseXmlElement("<root a=\"1\"/>"),
                    parseXmlElement("<root a=\"1\" b=\"2\"/>")));
        }
    }

    @Nested
    class ChildNodeComparison {
        @Test
        void identicalChildrenInLenientMode() {
            assertDoesNotThrow(() -> lenient.compare(
                    parseXmlElement("<root><child>text</child></root>"),
                    parseXmlElement("<root><child>text</child></root>")));
        }

        @Test
        void differentChildCountThrows() {
            assertThrows(MatchException.class, () -> lenient.compare(
                    parseXmlElement("<root><a/><b/></root>"),
                    parseXmlElement("<root><a/></root>")));
        }

        @Test
        void differentChildContentThrows() {
            assertThrows(MatchException.class, () -> lenient.compare(
                    parseXmlElement("<root><child>text1</child></root>"),
                    parseXmlElement("<root><child>text2</child></root>")));
        }

        @Test
        void nestedChildrenInLenientMode() {
            assertDoesNotThrow(() -> lenient.compare(
                    parseXmlElement("<root><a><b>val</b></a></root>"),
                    parseXmlElement("<root><a><b>val</b></a></root>")));
        }

        @Test
        void strictModeThrowsForNodesWithChildren() {
            // Strict mode check fires after child comparison
            assertThrows(MatchException.class, () -> strict.compare(
                    parseXmlElement("<root><child/></root>"),
                    parseXmlElement("<root><child/></root>")));
        }
    }

    @Nested
    class PatternMatchingInXml {
        @Test
        void patternInAttributeValue() {
            assertDoesNotThrow(() -> lenient.compare(
                    parseXmlElement("<root id=\"p(digit)\"/>"),
                    parseXmlElement("<root id=\"42\"/>")));
        }

        @Test
        void patternInTextContent() {
            assertDoesNotThrow(() -> lenient.compare(
                    parseXmlElement("<root><val>p(digit)</val></root>"),
                    parseXmlElement("<root><val>42</val></root>")));
        }
    }
}
