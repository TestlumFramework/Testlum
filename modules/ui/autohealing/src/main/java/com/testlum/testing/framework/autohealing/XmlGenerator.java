package com.testlum.testing.framework.autohealing;

import com.testlum.testing.framework.autohealing.dto.HealedLocators;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;

public class XmlGenerator {

    private static final String ROOT_TAG = "healedLocator";
    private static final String XPATH_TAG = "xpath";
    private static final String CSS_SELECTOR_TAG = "cssSelector";
    private static final String ID_TAG = "id";
    private static final String CLASS_NAME_TAG = "className";
    private static final String TEXT_TAG = "text";
    private static final String INDENT_AMOUNT_KEY = "{http://xml.apache.org/xslt}indent-amount";
    private static final String INDENT_AMOUNT = "2";

    public static String toXml(final HealedLocators healedLocators) {
        Document document = createDocument();
        Element root = document.createElement(ROOT_TAG);
        document.appendChild(root);
        appendSingle(document, root, ID_TAG, healedLocators.getId());
        appendSingle(document, root, CLASS_NAME_TAG, healedLocators.getClassName());
        appendSingle(document, root, TEXT_TAG, healedLocators.getText());
        appendMultiple(document, root, XPATH_TAG, healedLocators.getXpaths());
        appendMultiple(document, root, CSS_SELECTOR_TAG, healedLocators.getCssSelectors());
        return serialize(document);
    }

    private static Document createDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }

    private static void appendMultiple(final Document document, final Element parent, final String tagName,
                                       final List<String> tagValues) {
        if (tagValues == null || tagValues.isEmpty()) {
            return;
        }
        tagValues.forEach(tagValue -> appendSingle(document, parent, tagName, tagValue));
    }

    private static void appendSingle(final Document document, final Element parent, final String tagName,
                                     final String tagValue) {
        if (tagValue == null || tagValue.trim().isEmpty()) {
            return;
        }
        Element element = document.createElement(tagName);
        element.setTextContent(tagValue);
        parent.appendChild(element);
    }

    private static String serialize(final Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(INDENT_AMOUNT_KEY, INDENT_AMOUNT);
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }
}
