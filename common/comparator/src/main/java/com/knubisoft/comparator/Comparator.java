package com.knubisoft.comparator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knubisoft.comparator.exception.MatchException;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.knubisoft.comparator.util.LogMessage.CONTENT_DOES_MATCH;

public class Comparator extends AbstractObjectComparator<String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<ComparatorHandler> handlers = new ArrayList<>() {
        {
            add(new JsonComparatorHandler());
            add(new XmlComparatorHandler());
            add(new StringLinesComparatorHandler());
        }
    };

    public Comparator(final Mode mode) {
        super(mode);
    }

    public static Comparator strict() {
        return new Comparator(Mode.STRICT);
    }

    public static Comparator lenient() {
        return new Comparator(Mode.LENIENT);
    }

    public void compare(final String expected, final String actual) {
        final ComparisonRequest request = new ComparisonRequest(
                mode,
                getComparisonValue(expected),
                getComparisonValue(actual)
        );

        if (handlers.stream().noneMatch(handler -> handler.test(request))) {
            throw new MatchException(CONTENT_DOES_MATCH);
        }
    }

    private ComparisonRequest.ComparisonValue getComparisonValue(final String value) {
        return new ComparisonRequest.ComparisonValue(value, readJson(value), readXml(value));
    }

    private JsonNode readJson(final String value) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Node readXml(final String value) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            documentBuilder.setErrorHandler(new XmlErrorHandler());
            return documentBuilder.parse(new InputSource(new StringReader(value)));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return null;
        }
    }

    private interface ComparatorHandler extends Predicate<ComparisonRequest> {
    }

    private static class JsonComparatorHandler implements ComparatorHandler {

        @Override
        public boolean test(final ComparisonRequest request) {
            if (!request.getExpected().isJson() || !request.getActual().isJson()) {
                return false;
            }

            new JsonComparator(request.getMode()).compare(request.getExpected().getJson(),
                    request.getActual().getJson());

            return true;
        }
    }

    private static class XmlComparatorHandler implements ComparatorHandler {

        @Override
        public boolean test(final ComparisonRequest request) {
            if (!request.getExpected().isXml() || !request.getActual().isXml()) {
                return false;
            }

            new XMLComparator(request.getMode()).compare(request.getExpected().getXml(),
                    request.getActual().getXml());

            return true;
        }
    }

    private static class StringLinesComparatorHandler implements ComparatorHandler {

        @Override
        public boolean test(final ComparisonRequest request) {
            if (request.getExpected().isJson() || request.getExpected().isXml()
                    || request.getActual().isJson() || request.getActual().isXml()) {
                return false;
            }

            new StringLinesComparator(request.getMode()).compare(request.getExpected().getValue(),
                    request.getActual().getValue());

            return true;
        }
    }
}
