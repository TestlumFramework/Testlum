package com.testlum.testing.framework.mcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.testlum.testing.framework.mcp.exception.ToolException;
import com.testlum.testing.framework.mcp.constant.ToolMessage;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

/** The engine's XSDs are the syntax truth. They come from the folder named by TESTLUM_XSD_DIR. */
@Component
public final class EngineSchemas {

    private static final String SCENARIO_XSD = "scenario.xsd";
    private static final Pattern NS_BRACES = Pattern.compile(
            "\\{http://www\\.testlum\\.com/[^}]*}|\"http://www\\.testlum\\.com/[^\"]*\":");
    private static final int MAX_ISSUES = 20;

    private final String xsdDir;
    private final Map<String, Schema> compiled = new HashMap<>();
    private SchemaDescriber describer;

    public EngineSchemas(@Value("${TESTLUM_XSD_DIR:}") final String xsdDir) {
        this.xsdDir = xsdDir;
    }

    /** One validation finding. */
    public record Issue(String file, int line, int column, String level, String message) {
        @Override
        public String toString() {
            return file + (line > 0 ? ":" + line + ":" + column : "") + " [" + level + "] " + message;
        }
    }

    public Path dir() {
        if (xsdDir == null || xsdDir.isBlank()) {
            throw new ToolException(ToolMessage.NO_SCHEMAS);
        }
        Path dir = Path.of(xsdDir);
        if (!Files.isRegularFile(dir.resolve(SCENARIO_XSD))) {
            throw new ToolException(ToolMessage.XSD_DIR_WITHOUT_SCENARIO, dir);
        }
        return dir;
    }

    private synchronized Schema schema(final String xsd) {
        return compiled.computeIfAbsent(xsd, k -> {
            try {
                SchemaFactory f = SchemaFactory.newDefaultInstance();
                f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                f.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
                return f.newSchema(new StreamSource(dir().resolve(k).toFile()));
            } catch (SAXException e) {
                throw new ToolException(e, ToolMessage.CANNOT_COMPILE_SCHEMA, k, e.getMessage());
            }
        });
    }

    /** Validates a file against one of the engine schemas; returns findings, empty when valid. */
    public List<Issue> validate(final String xsd, final Path xml, final String shownName) {
        List<Issue> issues = new ArrayList<>();
        try {
            var v = schema(xsd).newValidator();
            v.setErrorHandler(new Collector(issues, shownName));
            v.validate(new StreamSource(xml.toFile()));
        } catch (SAXException | IOException e) {
            if (issues.isEmpty()) {
                issues.add(new Issue(shownName, 0, 0, "error", String.valueOf(e.getMessage())));
            }
        }
        return issues;
    }

    /** Validates global-config.xml, every integration.xml and every scenario.xml; PASS or FAIL then one line each. */
    public String checkFolder(final Path root, final String scenario, final String config) {
        List<Issue> issues = new ArrayList<>();
        Path configFile = root.resolve(config);
        if (Files.isRegularFile(configFile)) {
            issues.addAll(validate("global-config.xsd", configFile, config));
        } else {
            issues.add(new Issue(config, 0, 0, "error", "Global config not found, run testlum_scaffold first"));
        }
        checkIntegrations(root, configFile, issues);
        int checked = checkScenarios(root, scenario, issues);
        StringBuilder sb = new StringBuilder(issues.isEmpty() ? "PASS" : "FAIL").append(": ").append(issues.size())
                .append(" problems in ").append(checked).append(" scenarios");
        issues.forEach(i -> sb.append('\n').append(i));
        return sb.toString();
    }

    private int checkScenarios(final Path root, final String only, final List<Issue> issues) {
        Map<String, Path> scenarios = Layout.scenarioFiles(root);
        if (!only.isEmpty() && !scenarios.containsKey(only)) {
            issues.add(new Issue(Layout.SCENARIOS + "/" + only, 0, 0, "error", "No such scenario"));
            return 0;
        }
        scenarios.entrySet().stream().filter(e -> only.isEmpty() || e.getKey().equals(only))
                .forEach(e -> issues.addAll(validate(SCENARIO_XSD, e.getValue(), shown(root, e.getValue()))));
        return only.isEmpty() ? scenarios.size() : 1;
    }

    private static String shown(final Path root, final Path file) {
        return root.relativize(file).toString().replace('\\', '/');
    }

    private void checkIntegrations(final Path root, final Path configFile, final List<Issue> issues) {
        for (String env : enabledEnvironments(configFile)) {
            Path file = root.resolve(Layout.CONFIG).resolve(env).resolve(Layout.INTEGRATION_FILE);
            if (Files.isRegularFile(file)) {
                issues.addAll(validate("integration-config.xsd", file, shown(root, file)));
            } else {
                issues.add(new Issue(shown(root, file), 0, 0, "error",
                        "Integration file not found for the enabled environment " + env));
            }
        }
    }

    /** The folder of every enabled env in the global config; none when the file cannot be read. */
    private static List<String> enabledEnvironments(final Path configFile) {
        try {
            return XmlSupport.descendants(XmlSupport.parse(configFile).getDocumentElement(), "env").stream()
                    .filter(e -> !"false".equals(e.getAttribute("enabled"))).map(e -> e.getAttribute("folder"))
                    .filter(f -> !f.isBlank()).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /** The xmlns the root element of the given kind must carry. */
    public String namespaceOf(final String kind) {
        return describer().namespaceOf(kind);
    }

    /** Describes the element at a slash-separated path under the root of the given kind. */
    public String describe(final String kind, final String path) {
        return describer().describe(kind, path);
    }

    private synchronized SchemaDescriber describer() {
        if (describer == null) {
            describer = new SchemaDescriber(dir());
        }
        return describer;
    }

    /** Gathers parser findings, up to a cap, with the schema namespaces stripped from the text. */
    @RequiredArgsConstructor
    private static final class Collector implements ErrorHandler {
        private final List<Issue> issues;
        private final String shownName;

        @Override
        public void warning(final SAXParseException e) {
            add("warning", e);
        }

        @Override
        public void error(final SAXParseException e) {
            add("error", e);
        }

        @Override
        public void fatalError(final SAXParseException e) {
            add("error", e);
        }

        private void add(final String level, final SAXParseException e) {
            if (issues.size() < MAX_ISSUES) {
                issues.add(new Issue(shownName, e.getLineNumber(), e.getColumnNumber(), level,
                        NS_BRACES.matcher(String.valueOf(e.getMessage())).replaceAll("")));
            }
        }
    }
}
