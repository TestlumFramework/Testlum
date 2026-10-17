package com.testlum.testing.framework.mcp;

import com.testlum.testing.framework.mcp.exception.ToolException;
import com.testlum.testing.framework.mcp.constant.ToolMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Reads the engine's XSDs as plain DOM and returns the definition of one element for lookup. */
final class SchemaDescriber {

    private static final String XS = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    private final Map<String, Element> complexTypes = new HashMap<>();
    private final Map<String, Element> simpleTypes = new HashMap<>();
    private final Map<String, Element> topElements = new HashMap<>();
    private final Map<String, Element> groups = new HashMap<>();

    SchemaDescriber(final Path dir) {
        try (Stream<Path> s = Files.list(dir)) {
            for (Path p : s.filter(f -> f.toString().endsWith(".xsd")).toList()) {
                indexFile(p);
            }
        } catch (Exception e) {
            throw new ToolException(e, ToolMessage.CANNOT_INDEX_SCHEMAS, e.getMessage());
        }
    }

    private void indexFile(final Path p) throws Exception {
        Document d = parse(p);
        String ns = d.getDocumentElement().getAttribute("targetNamespace");
        for (Element e : children(d.getDocumentElement(), null)) {
            String key = ns + "|" + e.getAttribute("name");
            switch (e.getLocalName()) {
                case "complexType" -> complexTypes.put(key, e);
                case "simpleType" -> simpleTypes.put(key, e);
                case "group" -> groups.put(key, e);
                case "element" -> topElements.put(key, e);
                default -> { }
            }
        }
    }

    private static Document parse(final Path p) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return f.newDocumentBuilder().parse(p.toFile());
    }

    private static List<Element> children(final Element parent, final String local) {
        List<Element> out = new ArrayList<>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element e && XS.equals(e.getNamespaceURI())
                    && (local == null || local.equals(e.getLocalName()))) {
                out.add(e);
            }
        }
        return out;
    }

    /** Finds a named definition, resolving the prefix of qname in the scope of the element that uses it. */
    private static Element named(final Map<String, Element> defs, final Element user, final String qname) {
        int i = qname.indexOf(':');
        String ns = user.lookupNamespaceURI(i < 0 ? null : qname.substring(0, i));
        if (ns == null) {
            ns = user.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");
        }
        Element found = defs.get(ns + "|" + strip(qname));
        // a schema with no targetNamespace is included into the namespace of whoever includes it
        return found != null ? found : defs.get("|" + strip(qname));
    }

    private static String strip(final String qname) {
        int i = qname.indexOf(':');
        return i < 0 ? qname : qname.substring(i + 1);
    }

    private Element typeOf(final Element decl) {
        String t = decl.getAttribute("type");
        if (!t.isEmpty()) {
            return named(complexTypes, decl, t);
        }
        List<Element> inline = children(decl, "complexType");
        return inline.isEmpty() ? null : inline.get(0);
    }

    /** Root element name for each kind of file the engine reads. */
    private static String rootFor(final String kind) {
        return switch (kind) {
            case "scenario" -> "scenario";
            case "global-config" -> "globalTestConfiguration";
            case "integration" -> "integrations";
            default -> throw new ToolException(ToolMessage.BAD_KIND);
        };
    }

    /** The xmlns the root element of the given kind must carry. */
    String namespaceOf(final String kind) {
        String suffix = "|" + rootFor(kind);
        return topElements.keySet().stream().filter(k -> k.endsWith(suffix)).findFirst()
                .map(k -> k.substring(0, k.length() - suffix.length())).orElse("");
    }

    /** Describes the element at a slash-separated path under the root of the given kind. */
    String describe(final String kind, final String path) {
        String rootName = rootFor(kind);
        Element decl = topElements.entrySet().stream().filter(e -> e.getKey().endsWith("|" + rootName))
                .map(Map.Entry::getValue).findFirst()
                .orElseThrow(() -> new ToolException(ToolMessage.NO_ROOT_ELEMENT, rootName));
        List<String> steps = steps(path, rootName);
        String shown = rootName;
        for (String step : steps) {
            decl = child(decl, step, shown);
            shown = shown + "/" + step;
        }
        String out = shown + "\n" + fragment(decl);
        return steps.isEmpty() ? out + "\nxmlns: " + namespaceOf(kind) : out;
    }

    private static List<String> steps(final String path, final String rootName) {
        if (path == null || path.isBlank()) {
            return List.of();
        }
        List<String> steps = new ArrayList<>(Arrays.stream(path.trim().split("/")).filter(s -> !s.isBlank())
                .map(String::trim).toList());
        if (!steps.isEmpty() && steps.get(0).equals(rootName)) {
            steps.remove(0);
        }
        return steps;
    }

    private Element child(final Element decl, final String step, final String shown) {
        Element ct = typeOf(decl);
        if (ct == null) {
            throw new ToolException(ToolMessage.NO_CHILD_ELEMENTS, shown);
        }
        Element found = findElementDecl(ct, step);
        if (found == null) {
            throw new ToolException(ToolMessage.NO_SUCH_ELEMENT, step, shown, fragment(decl));
        }
        return found;
    }

    private Element findElementDecl(final Element node, final String name) {
        for (Element c : children(node, null)) {
            Element r = declIn(c, name);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    private Element declIn(final Element c, final String name) {
        return switch (c.getLocalName()) {
            case "element" -> name.equals(c.getAttribute("name")) ? c : null;
            case "group" -> inNamed(groups, c, "ref", name);
            case "extension" -> {
                Element fromBase = inNamed(complexTypes, c, "base", name);
                yield fromBase != null ? fromBase : findElementDecl(c, name);
            }
            case "complexContent", "simpleContent", "sequence", "choice", "all" -> findElementDecl(c, name);
            default -> null;
        };
    }

    private Element inNamed(final Map<String, Element> defs, final Element user, final String attr,
                            final String name) {
        Element def = named(defs, user, user.getAttribute(attr));
        return def == null ? null : findElementDecl(def, name);
    }

    /** The declaration plus the named types it needs, as XSD text: attributes, enumerations, patterns, children. */
    private String fragment(final Element decl) {
        Set<Element> defs = new LinkedHashSet<>(List.of(decl));
        Element ct = typeOf(decl);
        if (ct != null) {
            defs.add(ct);
            collect(ct, defs);
        } else if (!decl.getAttribute("type").isEmpty()) {
            Optional.ofNullable(named(simpleTypes, decl, decl.getAttribute("type"))).ifPresent(defs::add);
        }
        return defs.stream().map(XmlSupport::xml).collect(Collectors.joining("\n"));
    }

    private void collect(final Element node, final Set<Element> defs) {
        for (Element e : XmlSupport.descendants(node, "*")) {
            Element def = referenced(e);
            if (def != null && defs.add(def)) {
                collect(def, defs);
            }
        }
    }

    private Element referenced(final Element e) {
        return switch (e.getLocalName()) {
            case "extension", "restriction" -> baseOf(e);
            case "attribute" -> e.hasAttribute("type") ? named(simpleTypes, e, e.getAttribute("type")) : null;
            case "group" -> e.hasAttribute("ref") ? named(groups, e, e.getAttribute("ref")) : null;
            default -> null;
        };
    }

    private Element baseOf(final Element e) {
        Element base = named(complexTypes, e, e.getAttribute("base"));
        return base != null ? base : named(simpleTypes, e, e.getAttribute("base"));
    }
}
