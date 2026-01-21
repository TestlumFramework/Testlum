package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.autohealing.dto.HealedLocators;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.List;

public class LocatorXmlUpdater {

    @SneakyThrows
    public static void updateLocator(final LocatorData locatorData, final HealedLocators healedLocators) {
        Document document = constructDocument(locatorData);
        updateLocatorNode(locatorData, healedLocators, document);
        cleanEmptyTextNodes(document);
        flushToLocatorFile(locatorData, document);
    }

    private static Document constructDocument(final LocatorData locatorData)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(false);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(locatorData.getFile());
    }

    private static void updateLocatorNode(final LocatorData locatorData, final HealedLocators healedLocators,
                                          final Document document) {
        NodeList locatorNodes = document.getElementsByTagName("locator");
        for (int i = 0; i < locatorNodes.getLength(); i++) {
            Element locatorElement = (Element) locatorNodes.item(i);
            if (locatorElement.getAttribute("locatorId").equals(locatorData.getLocator().getLocatorId())) {
                addSingle(document, locatorElement, "id", healedLocators.getId());
                addSingle(document, locatorElement, "text", healedLocators.getText());
                addSingle(document, locatorElement, "className", healedLocators.getClassName());
                addMultiple(document, locatorElement, "xpath", healedLocators.getXpaths());
                addMultiple(document, locatorElement, "cssSelector", healedLocators.getCssSelectors());
            }
        }
    }

    private static void flushToLocatorFile(final LocatorData locatorData, final Document document)
            throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(locatorData.getFile());
        transformer.transform(source, result);
    }

    private static void cleanEmptyTextNodes(final Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
                node.removeChild(child);
            } else if (child.hasChildNodes()) {
                cleanEmptyTextNodes(child);
            }
        }
    }

    private static void addSingle(final Document document, final Element locatorElement,
                                  final String tagName, final String value) {
        if (value == null) {
            return;
        }
        Element tagElement = document.createElement(tagName);
        tagElement.setTextContent(value);
        locatorElement.appendChild(tagElement);
    }

    private static void addMultiple(final Document document, final Element locatorElement,
                                    final String tagName, final List<String> values) {
        for (String value : values) {
            Element tagElement = document.createElement(tagName);
            tagElement.setTextContent(value);
            locatorElement.appendChild(tagElement);
        }
    }

}
