package com.testlum.testing.framework.autohealing;

import com.testlum.testing.framework.autohealing.dto.HealedLocators;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.scenario.LocatorStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces a healed locator directly in the scenario xml for locators declared inline,
 * i.e. with any {@link LocatorStrategy} except {@code locatorId}.
 * The file is patched as plain text so that indentation, comments and the xml declaration stay untouched.
 */
public final class HealingScenarioUpdater {

    private static final Map<String, String> LOCATOR_ATTRIBUTES = Map.of(
            "locator", "locatorStrategy",
            "fromLocator", "fromLocatorStrategy",
            "toLocator", "toLocatorStrategy");

    private static final Pattern START_TAG =
            Pattern.compile("<[A-Za-z_][\\w.:-]*(?:\"[^\"]*\"|'[^']*'|[^>\"'])*>");
    private static final Pattern ATTRIBUTE =
            Pattern.compile("([A-Za-z_][\\w.:-]*)\\s*=\\s*(\"[^\"]*\"|'[^']*')");
    private static final Pattern IGNORED_REGION =
            Pattern.compile("<!--.*?-->|<!\\[CDATA\\[.*?]]>|<\\?.*?\\?>", Pattern.DOTALL);
    private static final Pattern NUMERIC_REFERENCE = Pattern.compile("&#(x?)([0-9a-fA-F]+);");

    private static final int DECIMAL_RADIX = 10;
    private static final int HEX_RADIX = 16;
    private static final char DOUBLE_QUOTE = '"';
    private static final String SELF_CLOSING = "/>";

    private HealingScenarioUpdater() {
    }

    public static int updateInlineLocator(final File scenarioFile,
                                          final String oldValue,
                                          final LocatorStrategy oldStrategy,
                                          final HealedLocator newLocator) {
        String content = readScenario(scenarioFile);
        UpdateResult result = replaceInContent(content, new Target(oldValue, oldStrategy, newLocator));
        if (result.getCount() > 0) {
            writeScenario(scenarioFile, result.getContent());
        }
        return result.getCount();
    }

    public static HealedLocator resolveNewLocator(final HealedLocators healedLocators,
                                                  final LocatorStrategy strategy) {
        String value = valueForStrategy(healedLocators, strategy);
        if (StringUtils.isNotBlank(value)) {
            return new HealedLocator(value, strategy);
        }
        String xpath = first(healedLocators.getXpaths());
        return StringUtils.isBlank(xpath) ? null : new HealedLocator(xpath, LocatorStrategy.XPATH);
    }

    private static String valueForStrategy(final HealedLocators healedLocators, final LocatorStrategy strategy) {
        return switch (strategy) {
            case XPATH -> first(healedLocators.getXpaths());
            case CSS_SELECTOR -> first(healedLocators.getCssSelectors());
            case ID -> healedLocators.getId();
            case CLASS -> healedLocators.getClassName();
            case TEXT -> healedLocators.getText();
            default -> null;
        };
    }

    private static String first(final List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private static UpdateResult replaceInContent(final String content, final Target target) {
        List<int[]> ignored = ignoredRegions(content);
        Matcher matcher = START_TAG.matcher(content);
        StringBuilder out = new StringBuilder();
        int count = 0;
        while (matcher.find()) {
            String replaced = isIgnored(ignored, matcher.start())
                    ? matcher.group() : replaceInTag(matcher.group(), target);
            count += replaced.equals(matcher.group()) ? 0 : 1;
            matcher.appendReplacement(out, Matcher.quoteReplacement(replaced));
        }
        return new UpdateResult(matcher.appendTail(out).toString(), count);
    }

    private static String replaceInTag(final String tag, final Target target) {
        String result = tag;
        for (Map.Entry<String, String> attributes : LOCATOR_ATTRIBUTES.entrySet()) {
            result = replaceAttributePair(result, attributes.getKey(), attributes.getValue(), target);
        }
        return result;
    }

    private static String replaceAttributePair(final String tag,
                                               final String locatorAttribute,
                                               final String strategyAttribute,
                                               final Target target) {
        String current = attributeValue(tag, locatorAttribute);
        if (current == null || !unescapeXml(current).equals(target.getOldValue())
                || effectiveStrategy(tag, strategyAttribute) != target.getOldStrategy()) {
            return tag;
        }
        String updated = setAttribute(tag, locatorAttribute, target.getNewLocator().getValue());
        LocatorStrategy newStrategy = target.getNewLocator().getStrategy();
        return newStrategy == target.getOldStrategy()
                ? updated : setAttribute(updated, strategyAttribute, newStrategy.value());
    }

    private static LocatorStrategy effectiveStrategy(final String tag, final String strategyAttribute) {
        String value = attributeValue(tag, strategyAttribute);
        if (value == null) {
            return LocatorStrategy.LOCATOR_ID;
        }
        try {
            return LocatorStrategy.fromValue(unescapeXml(value));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String attributeValue(final String tag, final String name) {
        Matcher matcher = ATTRIBUTE.matcher(tag);
        while (matcher.find()) {
            if (matcher.group(1).equals(name)) {
                String quoted = matcher.group(2);
                return quoted.substring(1, quoted.length() - 1);
            }
        }
        return null;
    }

    private static String setAttribute(final String tag, final String name, final String value) {
        Matcher matcher = ATTRIBUTE.matcher(tag);
        while (matcher.find()) {
            if (matcher.group(1).equals(name)) {
                char quote = matcher.group(2).charAt(0);
                String quoted = quote + escapeXml(value, quote) + quote;
                return tag.substring(0, matcher.start(2)) + quoted + tag.substring(matcher.end(2));
            }
        }
        return insertAttribute(tag, name, value);
    }

    private static String insertAttribute(final String tag, final String name, final String value) {
        int end = tag.endsWith(SELF_CLOSING) ? tag.length() - 2 : tag.length() - 1;
        String head = tag.substring(0, end);
        String separator = head.endsWith(StringUtils.SPACE) ? StringUtils.EMPTY : StringUtils.SPACE;
        String attribute = separator + name + "=" + DOUBLE_QUOTE
                + escapeXml(value, DOUBLE_QUOTE) + DOUBLE_QUOTE;
        return head + attribute + tag.substring(end);
    }

    private static String escapeXml(final String value, final char quote) {
        String escaped = value.replace("&", "&amp;").replace("<", "&lt;");
        return quote == DOUBLE_QUOTE
                ? escaped.replace("\"", "&quot;")
                : escaped.replace("'", "&apos;");
    }

    private static String unescapeXml(final String value) {
        return replaceNumericReferences(value)
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&");
    }

    private static String replaceNumericReferences(final String value) {
        Matcher matcher = NUMERIC_REFERENCE.matcher(value);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            int radix = matcher.group(1).isEmpty() ? DECIMAL_RADIX : HEX_RADIX;
            String character = String.valueOf((char) Integer.parseInt(matcher.group(2), radix));
            matcher.appendReplacement(out, Matcher.quoteReplacement(character));
        }
        return matcher.appendTail(out).toString();
    }

    private static List<int[]> ignoredRegions(final String content) {
        List<int[]> regions = new ArrayList<>();
        Matcher matcher = IGNORED_REGION.matcher(content);
        while (matcher.find()) {
            regions.add(new int[]{matcher.start(), matcher.end()});
        }
        return regions;
    }

    private static boolean isIgnored(final List<int[]> regions, final int position) {
        return regions.stream().anyMatch(region -> position >= region[0] && position < region[1]);
    }

    private static String readScenario(final File scenarioFile) {
        try {
            return FileUtils.readFileToString(scenarioFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }

    private static void writeScenario(final File scenarioFile, final String content) {
        try {
            FileUtils.writeStringToFile(scenarioFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static final class HealedLocator {
        private final String value;
        private final LocatorStrategy strategy;
    }

    @Getter
    @RequiredArgsConstructor
    private static final class Target {
        private final String oldValue;
        private final LocatorStrategy oldStrategy;
        private final HealedLocator newLocator;
    }

    @Getter
    @RequiredArgsConstructor
    private static final class UpdateResult {
        private final String content;
        private final int count;
    }
}
