package com.knubisoft.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link ComparisonRequest} verifying ComparisonValue type detection and field access. */
class ComparisonRequestTest {

    @Test
    void isJsonReturnsTrueWhenJsonNodePresent() throws Exception {
        JsonNode json = new ObjectMapper().readTree("{\"a\":1}");
        ComparisonRequest.ComparisonValue value = new ComparisonRequest.ComparisonValue("{\"a\":1}", json, null);
        assertTrue(value.isJson());
        assertFalse(value.isXml());
    }

    @Test
    void isXmlReturnsTrueWhenXmlNodePresent() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Node xml = factory.newDocumentBuilder().parse(new InputSource(new StringReader("<root/>")));
        ComparisonRequest.ComparisonValue value = new ComparisonRequest.ComparisonValue("<root/>", null, xml);
        assertFalse(value.isJson());
        assertTrue(value.isXml());
    }

    @Test
    void neitherJsonNorXml() {
        ComparisonRequest.ComparisonValue value = new ComparisonRequest.ComparisonValue("plain text", null, null);
        assertFalse(value.isJson());
        assertFalse(value.isXml());
    }

    @Test
    void getValueReturnsRawString() {
        ComparisonRequest.ComparisonValue value = new ComparisonRequest.ComparisonValue("test", null, null);
        assertEquals("test", value.getValue());
    }

    @Test
    void comparisonRequestHoldsAllFields() throws Exception {
        JsonNode json = new ObjectMapper().readTree("{}");
        ComparisonRequest.ComparisonValue expected = new ComparisonRequest.ComparisonValue("{}", json, null);
        ComparisonRequest.ComparisonValue actual = new ComparisonRequest.ComparisonValue("{}", json, null);
        ComparisonRequest request = new ComparisonRequest(Mode.STRICT, expected, actual);

        assertEquals(Mode.STRICT, request.getMode());
        assertEquals(expected, request.getExpected());
        assertEquals(actual, request.getActual());
    }
}
