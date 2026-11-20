package com.testlum.testing.framework.mcp;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** What the engine's report/junit.xml says: the testcase elements of its testsuites, and the suites' own counts. */
final class JunitResults {

    private static final String FAILURE = "failure";
    private static final String ERROR = "error";
    private static final String SKIPPED = "skipped";
    private static final int MAX_FAILURE_CHARS = 4000;

    private final List<Element> cases;
    private final int suiteFailures;

    private JunitResults(final List<Element> cases, final int suiteFailures) {
        this.cases = cases;
        this.suiteFailures = suiteFailures;
    }

    /** The results in the file, or none when it is missing. */
    static JunitResults read(final Path file) throws Exception {
        if (!Files.isRegularFile(file)) {
            return new JunitResults(List.of(), 0);
        }
        List<Element> suites = suites(XmlSupport.parse(file).getDocumentElement());
        List<Element> cases = new ArrayList<>();
        suites.forEach(suite -> cases.addAll(testcases(suite)));
        int declared = suites.stream().mapToInt(s -> number(s, "failures") + number(s, ERROR + "s")).sum();
        return new JunitResults(cases, declared);
    }

    private static List<Element> suites(final Element top) {
        List<Element> suites = new ArrayList<>(XmlSupport.descendants(top, "testsuite"));
        if ("testsuite".equals(top.getLocalName())) {
            suites.add(0, top);
        }
        return suites;
    }

    private static List<Element> testcases(final Element suite) {
        List<Element> out = new ArrayList<>();
        for (Node n = suite.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && "testcase".equals(e.getLocalName())) {
                out.add(e);
            }
        }
        return out;
    }

    private static int number(final Element e, final String attribute) {
        try {
            return Math.max(0, Integer.parseInt(e.getAttribute(attribute).trim()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    boolean isEmpty() {
        return cases.isEmpty();
    }

    /** Runs that were not skipped. */
    int executed() {
        return cases.size() - count(SKIPPED);
    }

    /** The failed testcases, or the failures a suite declares when it shows fewer. */
    int failed() {
        return Math.max(failedCases(), suiteFailures);
    }

    private int failedCases() {
        return (int) cases.stream().filter(tc -> failure(tc) != null).count();
    }

    private int count(final String child) {
        return (int) cases.stream().filter(tc -> failure(tc) == null && XmlSupport.child(tc, child) != null).count();
    }

    /** The counts, then one line per scenario run (PASS, FAIL or SKIP) and the full text of each failure. */
    String summary() {
        int skipped = count(SKIPPED);
        StringBuilder lines = new StringBuilder();
        StringBuilder failures = new StringBuilder();
        for (Element tc : cases) {
            Element f = failure(tc);
            lines.append(f != null ? "FAIL " : XmlSupport.child(tc, SKIPPED) != null ? "SKIP " : "PASS ")
                    .append(tc.getAttribute("name")).append('\n');
            failures.append(f == null ? "" : failureText(tc, f));
        }
        return "scenarios: " + cases.size() + " total, " + (cases.size() - skipped - failedCases()) + " passed, "
                + failed() + " failed, " + skipped + " skipped\n" + lines + failures;
    }

    private static Element failure(final Element tc) {
        Element f = XmlSupport.child(tc, FAILURE);
        return f != null ? f : XmlSupport.child(tc, ERROR);
    }

    private static String failureText(final Element tc, final Element f) {
        String text = f.getTextContent().isBlank() ? f.getAttribute("message") : f.getTextContent();
        String cut = text.length() <= MAX_FAILURE_CHARS ? text.strip()
                : text.substring(0, MAX_FAILURE_CHARS).strip() + "\n[cut]";
        return "--- " + tc.getAttribute("name") + " (" + tc.getAttribute("classname") + ")\n" + cut + "\n";
    }
}
