package com.knubisoft.testlum.testing.framework.autohealing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knubisoft.testlum.testing.framework.autohealing.dto.HealedLocators;
import com.knubisoft.testlum.testing.framework.autohealing.dto.HealingElementMetadata;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.framework.util.LocatorXmlUpdater;
import com.knubisoft.testlum.testing.model.global_config.AutoHealingMode;
import com.knubisoft.testlum.testing.model.pages.Locator;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.Charset.defaultCharset;

public class LocatorAutohealer {

    private static final double MIN_ACCEPTABLE_SCORE = 0.35;
    private static final double STRUCTURAL_MAX_WEIGHT = 0.3;
    private static final double STRUCTURAL_WEIGHT_PER_SIGNAL = 0.10;
    private static final int MAX_CANDIDATES_FOR_STRUCTURAL_CHECK = 40;
    private static final Pattern DYNAMIC_VALUE_PATTERN = Pattern.compile(
            ".*[_-][a-f0-9]{5,}$"
            + "|^[a-f0-9]{8,}$"
            + "|.*\\d{10,}.*"
            + "|.*__[A-Za-z0-9]{5,}$");
    private static final List<String> FALLBACK_TAGS =
            List.of("input", "button", "a", "select", "textarea", "div", "span", "label");

    private static final String PATCH_FILE_PREFIX = "patch_";
    private static final String PATCH_FILE_EXTENSION = ".xml";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
    private final LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();
    private final ElementMetadataExtractor elementMetadataExtractor;
    private final WebDriver driver;

    public LocatorAutohealer(final ExecutorDependencies dependencies) {
        this.elementMetadataExtractor = dependencies.getContext().getBean(ElementMetadataExtractor.class);
        this.driver = dependencies.getDriver();
    }

    // CHECKSTYLE:OFF
    public Optional<WebElement> heal(final Locator locator) {
        HealingElementMetadata metadata = elementMetadataExtractor.extractMetadata(locator);
        List<WebElement> candidates = collectCandidates(metadata);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        List<ScoredElement> baseScored = candidates.stream()
                .map(candidate -> new ScoredElement(candidate, calculateWeightedBaseScore(metadata, candidate)))
                .sorted(Comparator.comparingDouble((ScoredElement s) -> s.score).reversed())
                .toList();

        int structuralLimit = Math.min(MAX_CANDIDATES_FOR_STRUCTURAL_CHECK, baseScored.size());

        ScoredElement best = null;
        for (int i = 0; i < baseScored.size(); i++) {
            ScoredElement scored = baseScored.get(i);
            double finalScore = scored.score;
            if (i < structuralLimit) {
                finalScore = combineWithStructuralScore(scored.score, metadata, scored.element);
            }
            if (finalScore < MIN_ACCEPTABLE_SCORE) {
                continue;
            }
            if (best == null || finalScore > best.score) {
                best = new ScoredElement(scored.element, finalScore);
            }
        }

        return Optional.ofNullable(best).map(ScoredElement::getElement);
    }
    // CHECKSTYLE:ON

    public File generateNewLocators(final WebElement healedElement, final AutoHealingMode mode,
                                    final ExecutorDependencies dependencies, final LocatorData locatorData) {
        HealedLocators healedLocators = generateXpathAndCssSelector(healedElement);
        generateText(healedElement, healedLocators);
        generateId(healedElement, healedLocators);
        generateClass(healedElement, healedLocators);
        if (mode == AutoHealingMode.SOFT) {
            return generatePatchForLocatorDefinedInScenario(
                  dependencies, healedLocators, locatorData.getFile() != null, locatorData);
        } else {
            if (locatorData.getFile() != null) {
                LocatorXmlUpdater.updateLocator(locatorData, healedLocators);
            }
        }
        return locatorData.getFile();
    }

    private List<WebElement> collectCandidates(final HealingElementMetadata metadata) {
        List<String> tags = resolveTags(metadata);
        Set<WebElement> uniqueCandidates = new LinkedHashSet<>();
        for (String anchorId : metadata.getAncestorIds()) {
            collectCandidatesByAncestorId(anchorId, tags, uniqueCandidates);
        }
        for (String tag : tags) {
            uniqueCandidates.addAll(driver.findElements(By.tagName(tag)));
        }
        uniqueCandidates.addAll(collectShadowDomCandidates(tags));
        return new ArrayList<>(uniqueCandidates);
    }

    // CHECKSTYLE:OFF
    private List<WebElement> collectShadowDomCandidates(final List<String> tags) {
        try {
            String script =
                    "var results = [];"
                    + "var tags = arguments[0];"
                    + "function traverse(root) {"
                    + "  for (var t = 0; t < tags.length; t++) {"
                    + "    var els = root.querySelectorAll(tags[t]);"
                    + "    for (var i = 0; i < els.length; i++) results.push(els[i]);"
                    + "  }"
                    + "  var all = root.querySelectorAll('*');"
                    + "  for (var i = 0; i < all.length; i++) {"
                    + "    if (all[i].shadowRoot) traverse(all[i].shadowRoot);"
                    + "  }"
                    + "}"
                    + "var all = document.querySelectorAll('*');"
                    + "for (var i = 0; i < all.length; i++) {"
                    + "  if (all[i].shadowRoot) traverse(all[i].shadowRoot);"
                    + "}"
                    + "return results;";
            List<WebElement> shadowElements = (List<WebElement>) ((JavascriptExecutor) driver)
                    .executeScript(script, tags);
            return shadowElements != null ? shadowElements : List.of();
        } catch (Exception ignored) {
            return List.of();
        }
    }
    // CHECKSTYLE:ON

    private void collectCandidatesByAncestorId(final String anchorId, final List<String> tags,
                                               final Set<WebElement> uniqueCandidates) {
        try {
            SearchContext anchor = driver.findElement(By.id(anchorId));
            for (String tag : tags) {
                uniqueCandidates.addAll(anchor.findElements(By.tagName(tag)));
            }
        } catch (NoSuchElementException ignored) {
            // ignored
        }
    }

    private List<String> resolveTags(final HealingElementMetadata metadata) {
        Optional<String> mostCommonTag = metadata.findMostCommonTag();
        if (mostCommonTag.isPresent() && !"*".equals(mostCommonTag.get())) {
            return List.of(mostCommonTag.get().toLowerCase());
        }

        List<String> extractedTags = metadata.getTagNames().stream()
                .map(String::toLowerCase)
                .filter(tag -> !"*".equals(tag))
                .distinct()
                .collect(Collectors.toList());

        return extractedTags.isEmpty() ? FALLBACK_TAGS : extractedTags;
    }

    private double combineWithStructuralScore(final double baseScore,
                                              final HealingElementMetadata metadata,
                                              final WebElement candidate) {
        StructuralInfo structuralInfo = calculateStructuralInfo(metadata, candidate);
        if (structuralInfo.checksPerformed == 0) {
            return baseScore;
        }

        int signalCount = countAvailableSignals(metadata);
        double adaptiveWeight = Math.min(STRUCTURAL_MAX_WEIGHT, signalCount * STRUCTURAL_WEIGHT_PER_SIGNAL);

        double structuralScore = structuralInfo.matchedChecks / (double) structuralInfo.checksPerformed;
        return (baseScore * (1 - adaptiveWeight)) + (structuralScore * adaptiveWeight);
    }

    // CHECKSTYLE:OFF
    private StructuralInfo calculateStructuralInfo(final HealingElementMetadata metadata, final WebElement candidate) {
        int matchedChecks = 0;
        int checksPerformed = 0;

        if (!metadata.getParentTags().isEmpty()) {
            checksPerformed++;
            try {
                String actualParentTag = (String) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].parentNode && arguments[0].parentNode.tagName " +
                                       "? arguments[0].parentNode.tagName.toLowerCase() : null;", candidate);
                if (actualParentTag != null && metadata.getParentTags().contains(actualParentTag)) {
                    matchedChecks++;
                }
            } catch (Exception ignored) {
                // ignored
            }
        }

        if (!metadata.getAncestorIds().isEmpty()) {
            checksPerformed++;
            try {
                Boolean isInsideAnchor = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "var el = arguments[0];"
                                + "var ids = arguments[1];"
                                + "return ids.some(function(id){"
                                + "  var anchor = document.getElementById(id);"
                                + "  return anchor && anchor.contains(el);"
                                + "});",
                        candidate,
                        new ArrayList<>(metadata.getAncestorIds())
                );
                if (Boolean.TRUE.equals(isInsideAnchor)) {
                    matchedChecks++;
                }
            } catch (Exception ignored) {
                // ignored
            }
        }

        return new StructuralInfo(matchedChecks, checksPerformed);
    }
    // CHECKSTYLE:ON

    private File generatePatchForLocatorDefinedInScenario(final ExecutorDependencies dependencies,
                                                          final HealedLocators healedLocators,
                                                          final boolean isLocatorDefinedInLocatorFile,
                                                          final LocatorData locatorData) {
        File directory;
        String fileName;
        if (isLocatorDefinedInLocatorFile) {
            directory = locatorData.getFile().getParentFile();
            fileName = PATCH_FILE_PREFIX + locatorData.getLocator().getLocatorId() + PATCH_FILE_EXTENSION;
        } else {
            directory = dependencies.getFile().getParentFile();
            int position = dependencies.getPosition().get();
            fileName = PATCH_FILE_PREFIX + position + PATCH_FILE_EXTENSION;
        }
        File patch = new File(directory, fileName);
        writeHealedLocatorsToFile(healedLocators, patch);
        return patch;
    }

    private void writeHealedLocatorsToFile(final HealedLocators healedLocators, final File patch) {
        String xmlContent = XmlGenerator.toXml(healedLocators);
        try {
            FileUtils.writeStringToFile(patch, xmlContent, defaultCharset());
        } catch (IOException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
    }

    private void generateClass(final WebElement healedElement, final HealedLocators healedLocators) {
        String className = healedElement.getAttribute("class");
        if (className != null && !className.trim().isEmpty()) {
            healedLocators.setClassName(className.trim());
        }
    }

    private void generateId(final WebElement healedElement, final HealedLocators healedLocators) {
        String id = healedElement.getAttribute("id");
        if (id != null && !id.trim().isEmpty()) {
            healedLocators.setId(id.trim());
        }
    }

    private void generateText(final WebElement healedElement, final HealedLocators healedLocators) {
        String text = normalize(healedElement.getText());
        if (!text.isEmpty()) {
            healedLocators.setText(text);
        }
    }

    // CHECKSTYLE:OFF
    private double calculateWeightedBaseScore(final HealingElementMetadata metadata, final WebElement candidate) {
        double totalWeight = 0;
        double weightedScore = 0;

        if (!metadata.getTexts().isEmpty()) {
            double textScore = calculateTextScore(metadata.getTexts(), candidate);
            weightedScore += textScore * 0.45;
            totalWeight += 0.45;
        }

        if (!metadata.getIds().isEmpty()) {
            double idScore = calculateIdScore(metadata.getIds(), candidate);
            weightedScore += idScore * 0.30;
            totalWeight += 0.30;
        }

        if (!metadata.getAttributes().isEmpty()) {
            double attrScore = calculateAttributeScore(metadata.getAttributes(), candidate);
            weightedScore += attrScore * 0.15;
            totalWeight += 0.15;
        }

        if (!metadata.getClasses().isEmpty()) {
            double classScore = calculateClassScore(metadata.getClasses(), candidate);
            weightedScore += classScore * 0.10;
            totalWeight += 0.10;
        }

        if (totalWeight == 0) {
            return 0;
        }
        return boundToUnitInterval(weightedScore / totalWeight);
    }

    private double calculateTextScore(final Collection<String> texts, final WebElement candidate) {
        String candidateText = normalize(candidate.getText());
        if (candidateText.isEmpty()) {
            return 0;
        }
        return maxSimilarity(texts, candidateText);
    }

    private double calculateIdScore(final Collection<String> ids, final WebElement candidate) {
        String candidateId = normalize(candidate.getAttribute("id"));
        if (candidateId.isEmpty() || isDynamicValue(candidateId)) {
            return 0;
        }

        List<String> stableIds = ids.stream()
                .filter(id -> !isDynamicValue(id))
                .toList();
        if (stableIds.isEmpty()) {
            return 0;
        }

        boolean exactMatch = stableIds.stream()
                .map(this::normalize)
                .anyMatch(candidateId::equals);
        if (exactMatch) {
            return 1.0;
        }

        return maxSimilarity(stableIds, candidateId) * 0.85;
    }

    private double calculateClassScore(final Collection<String> classes, final WebElement candidate) {
        String candidateClass = normalize(candidate.getAttribute("class"));
        if (candidateClass.isEmpty()) {
            return 0;
        }

        Set<String> candidateTokens = filterStableTokens(candidateClass.split("\\s+"));
        if (candidateTokens.isEmpty()) {
            return 0;
        }

        double best = 0;
        for (String cls : classes) {
            String normalized = normalize(cls);
            if (normalized.isEmpty()) {
                continue;
            }
            Set<String> sourceTokens = filterStableTokens(normalized.split("\\s+"));
            if (sourceTokens.isEmpty()) {
                continue;
            }

            long intersection = sourceTokens.stream().filter(candidateTokens::contains).count();
            long union = sourceTokens.size() + candidateTokens.size() - intersection;
            double tokenSimilarity = union == 0 ? 0 : (double) intersection / union;
            double fuzzySimilarity = getScore(normalized, candidateClass);
            best = Math.max(best, (tokenSimilarity * 0.6) + (fuzzySimilarity * 0.4));
        }

        return boundToUnitInterval(best);
    }

    private Set<String> filterStableTokens(final String[] tokens) {
        Set<String> stable = new HashSet<>();
        for (String token : tokens) {
            if (!token.isEmpty() && !isDynamicValue(token)) {
                stable.add(token);
            }
        }
        return stable;
    }

    private double maxSimilarity(final Collection<String> items, final String target) {
        if (items.isEmpty()) {
            return 0.0;
        }
        String normalizedTarget = normalize(target);
        if (normalizedTarget.isEmpty()) {
            return 0.0;
        }

        return items.stream()
                .map(this::normalize)
                .filter(value -> !value.isEmpty())
                .mapToDouble(item -> this.getScore(item, normalizedTarget))
                .max().orElse(0.0);
    }

    private double calculateAttributeScore(final Map<String, String> metaAttrs, final WebElement candidate) {
        if (metaAttrs.isEmpty()) {
            return 0.0;
        }

        double total = 0;
        int comparedCount = 0;
        for (Map.Entry<String, String> entry : metaAttrs.entrySet()) {
            String expected = normalize(entry.getValue());
            if (expected.isEmpty()) {
                continue;
            }

            String actual = normalize(candidate.getAttribute(entry.getKey()));
            if (actual.isEmpty()) {
                continue;
            }

            comparedCount++;
            total += getScore(expected, actual);
        }

        if (comparedCount == 0) {
            return 0.0;
        }
        return boundToUnitInterval(total / comparedCount);
    }
    // CHECKSTYLE:ON

    private String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private double boundToUnitInterval(final double value) {
        return Math.max(0, Math.min(1, value));
    }

    private double getScore(final String previousValue, final String actualValue) {
        Double jaroWinklerSimilarityScore = jaroWinklerSimilarity.apply(previousValue, actualValue);

        double normalizedLevenshteinDistance = getNormalizedLevenshteinDistance(previousValue, actualValue);

        return Math.max(jaroWinklerSimilarityScore, normalizedLevenshteinDistance);
    }

    private double getNormalizedLevenshteinDistance(final String previousValue, final String actualValue) {
        int maxLength = Math.max(previousValue.length(), actualValue.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance.apply(previousValue, actualValue);
        return 1.0 - ((double) distance / maxLength);
    }

    // CHECKSTYLE:OFF
    private HealedLocators generateXpathAndCssSelector(final WebElement healedElement) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script =
                "var el = arguments[0];" +
                "var results = { xpaths: [], cssSelectors: [] };" +

                "function escapeXPathAttr(val) {" +
                "  if (val.indexOf('\"') === -1) return '\"' + val + '\"';" +
                "  if (val.indexOf(\"'\") === -1) return \"'\" + val + \"'\";" +
                "  return 'concat(\"' + val.replace(/\"/g, '\",\\'\"\\',\"') + '\")';" +
                "}" +

                "var attrs = ['name', 'placeholder', 'type', 'role', 'aria-label', 'data-testid'];" +
                "attrs.forEach(function(attr) {" +
                "  var val = el.getAttribute(attr);" +
                "  if(val) {" +
                "    results.xpaths.push('//' + el.tagName.toLowerCase() + '[@' + attr + '=' + " +
                "escapeXPathAttr(val) + ']');" +
                "  }" +
                "});" +

                "function getRelativeXPath(node) {" +
                "  var parts = [];" +
                "  while (node && node.nodeType === Node.ELEMENT_NODE) {" +
                "    if (node.id) {" +
                "      parts.unshift('//*[@id=' + escapeXPathAttr(node.id) + ']');" +
                "      return parts.join('/');" +
                "    }" +
                "    var index = 1;" +
                "    for (var sib = node.previousSibling; sib; sib = sib.previousSibling) {" +
                "      if (sib.nodeName == node.nodeName) index++;" +
                "    }" +
                "    parts.unshift(node.tagName.toLowerCase() + '[' + index + ']');" +
                "    node = node.parentNode;" +
                "  }" +
                "  return '/' + parts.join('/');" +
                "}" +
                "results.xpaths.push(getRelativeXPath(el));" +

                "var cn = (typeof el.className === 'string') ? el.className " +
                "  : (el.className && el.className.baseVal != null ? el.className.baseVal : '');" +
                "if (cn && cn.trim()) {" +
                "  var classes = cn.trim().split(/\\s+/).join('.');" +
                "  results.cssSelectors.push(el.tagName.toLowerCase() + '.' + classes);" +
                "}" +

                "return JSON.stringify(results);";
        String xpathAndClassNames = (String) js.executeScript(script, healedElement);
        try {
            return OBJECT_MAPPER.readValue(xpathAndClassNames, HealedLocators.class);
        } catch (JsonProcessingException e) {
            return new HealedLocators();
        }
    }

    private int countAvailableSignals(final HealingElementMetadata metadata) {
        int count = 0;
        if (!metadata.getTexts().isEmpty()) {
            count++;
        }
        if (!metadata.getIds().isEmpty()) {
            count++;
        }
        if (!metadata.getAttributes().isEmpty()) {
            count++;
        }
        if (!metadata.getClasses().isEmpty()) {
            count++;
        }
        return count;
    }
    // CHECKSTYLE:ON

    private boolean isDynamicValue(final String value) {
        return value != null && DYNAMIC_VALUE_PATTERN.matcher(value.trim()).matches();
    }

    private static class ScoredElement {
        @Getter
        WebElement element;
        double score;

        ScoredElement(final WebElement element, final double score) {
            this.element = element;
            this.score = score;
        }
    }

    private static class StructuralInfo {
        int matchedChecks;
        int checksPerformed;

        StructuralInfo(final int matchedChecks, final int checksPerformed) {
            this.matchedChecks = matchedChecks;
            this.checksPerformed = checksPerformed;
        }
    }
}
