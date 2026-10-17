package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.constant.ToolMessage;
import com.testlum.testing.framework.mcp.exception.ToolException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Small DOM helpers shared by the schema lookup, the checks and the runner. */
final class XmlSupport {

    private XmlSupport() {
    }

    static String esc(final String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    static Document parse(final Path file) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var builder = f.newDocumentBuilder();
        builder.setErrorHandler(null); // exceptions carry the message, so keep the parser quiet on stderr
        return builder.parse(file.toFile());
    }

    /** One element as indented text, without the namespace declarations the DOM copies onto it. */
    static String xml(final Element e) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter out = new StringWriter();
            t.transform(new DOMSource(e), new StreamResult(out));
            return out.toString().replaceAll(" xmlns(:\\w+)?=\"[^\"]*\"", "").replaceAll("(?m)^\\s*\\R", "").strip();
        } catch (TransformerException ex) {
            throw new ToolException(ex, ToolMessage.CANNOT_SHOW_SCHEMA, ex.getMessage());
        }
    }

    static List<Element> descendants(final Element root, final String local) {
        List<Element> out = new ArrayList<>();
        NodeList nl = root.getElementsByTagNameNS("*", local);
        for (int i = 0; i < nl.getLength(); i++) {
            out.add((Element) nl.item(i));
        }
        return out;
    }

    static Element child(final Element parent, final String local) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && local.equals(e.getLocalName())) {
                return e;
            }
        }
        return null;
    }
}
