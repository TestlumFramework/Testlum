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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.Charset.defaultCharset;


public class LocatorAutohealer {

    private static final double MIN_ACCEPTABLE_SCORE = 0.5;
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

    public Optional<WebElement> heal(final Locator locator) {
        HealingElementMetadata healingElementMetadata = elementMetadataExtractor.extractMetadata(locator);
        String tag = healingElementMetadata.findMostCommonTag().orElse("*");
        SearchContext searchScope = driver;
        for (String anchorId : healingElementMetadata.getAncestorIds()) {
            try {
                searchScope = driver.findElement(By.id(anchorId));
                break;
            } catch (NoSuchElementException ignored) {
            }
        }
        List<WebElement> candidates = searchScope.findElements(By.tagName(tag));

        return candidates.stream()
                .map(candidate -> new ScoredElement(candidate, calculateScore(healingElementMetadata, candidate)))
                .filter(scored -> scored.score >= MIN_ACCEPTABLE_SCORE)
                .max(Comparator.comparingDouble(s -> s.score))
                .map(ScoredElement::getElement);
    }

    public File generateNewLocators(final WebElement healedElement, final AutoHealingMode mode,
                                    final ExecutorDependencies dependencies, LocatorData locatorData) {
        HealedLocators healedLocators = generateXpathAndCssSelector(healedElement);
        generateText(healedElement, healedLocators);
        generateId(healedElement, healedLocators);
        generateClass(healedElement, healedLocators);

        if (mode == AutoHealingMode.SOFT) {
            return generatePatchForLocatorDefinedInScenario(
                    dependencies, healedLocators, locatorData.getFile() != null, locatorData
            );
        } else {
            if (locatorData.getFile() != null) {
                LocatorXmlUpdater.updateLocator(locatorData, healedLocators);
            }
        }
        return locatorData.getFile();
    }

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
        String xmlContent = XmlGenerator.toXml(healedLocators);
        try {
            FileUtils.writeStringToFile(patch, xmlContent, defaultCharset());
        } catch (IOException e) {
            throw new DefaultFrameworkException(e.getMessage());
        }
        return patch;
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
        String text = healedElement.getText().trim();
        if (!text.isEmpty()) {
            healedLocators.setText(text);
        }
    }

    private double calculateScore(final HealingElementMetadata metadata, final WebElement candidate) {
        double baseScore = this.calculateWeightedBaseScore(metadata, candidate);

        double structuralScore = 0;
        double structuralWeight = 0.2;
        int structuralChecksPerformed = 0;

        try {
            String actualParentTag = (String) ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].parentNode.tagName;", candidate);

            if (actualParentTag != null && !metadata.getParentTags().isEmpty()) {
                structuralChecksPerformed++;
                if (metadata.getParentTags().contains(actualParentTag.toLowerCase())) {
                    structuralScore += 1.0;
                }
            }
        } catch (Exception ignored) {
        }

        if (!metadata.getAncestorIds().isEmpty()) {
            structuralChecksPerformed++;
            boolean isInsideAnchor = metadata.getAncestorIds().stream()
                    .anyMatch(anchorId -> {
                        try {
                            return (Boolean) ((JavascriptExecutor) driver).executeScript(
                                    "return document.getElementById(arguments[1])?.contains(arguments[0]);",
                                    candidate, anchorId);
                        } catch (Exception e) {
                            return false;
                        }
                    });
            if (isInsideAnchor) {
                structuralScore += 1.0;
            }
        }

        double finalStructuralScore = (structuralChecksPerformed > 0)
                ? (structuralScore / structuralChecksPerformed)
                : 0;

        return (baseScore * (1 - structuralWeight)) + (finalStructuralScore * structuralWeight);
    }

    private double calculateWeightedBaseScore(final HealingElementMetadata metadata, final WebElement candidate) {
        double totalWeight = 0;
        double weightedScore = 0;

        String candidateText = candidate.getText();
        if (!metadata.getTexts().isEmpty() && !candidateText.isEmpty()) {
            weightedScore += this.maxSimilarity(metadata.getTexts(), candidateText) * 0.5;
            totalWeight += 0.5;
        }

        String candidateId = candidate.getAttribute("id");
        if (!metadata.getIds().isEmpty() && candidateId != null && !candidateId.isEmpty()) {
            weightedScore += this.maxSimilarity(metadata.getIds(), candidateId) * 0.3;
            totalWeight += 0.3;
        }

        if (!metadata.getAttributes().isEmpty()) {
            double attrScore = this.calculateAttributeScore(metadata.getAttributes(), candidate);
            if (attrScore > 0) {
                weightedScore += attrScore * 0.2;
                totalWeight += 0.2;
            }
        }

        String candidateClass = candidate.getAttribute("class");
        if (!metadata.getClasses().isEmpty() && candidateClass != null && !candidateClass.isEmpty()) {
            weightedScore += this.maxSimilarity(metadata.getClasses(), candidateClass) * 0.1;
            totalWeight += 0.1;
        }

        return (totalWeight == 0) ? 0 : (weightedScore / totalWeight);
    }

    private double maxSimilarity(final Collection<String> items, final String target) {
        if (items.isEmpty() || target == null || target.isEmpty()) return 0.0;
        return items.stream()
                .mapToDouble(item -> this.getScore(item.toLowerCase(), target.toLowerCase()))
                .max().orElse(0.0);
    }

    private double calculateAttributeScore(final Map<String, String> metaAttrs, final WebElement candidate) {
        if (metaAttrs.isEmpty()) {
            return 0.0;
        }
        double totalMatch = 0.0;
        for (Map.Entry<String, String> entry : metaAttrs.entrySet()) {
            String actualValue = candidate.getAttribute(entry.getKey());
            if (actualValue != null) {
                totalMatch += this.getScore(entry.getValue(), actualValue);
            }
        }
        return totalMatch / metaAttrs.size();
    }

    private double getScore(final String previousValue, final String actualValue) {
        Double jaroWinklerSimilarityScore = jaroWinklerSimilarity.apply(previousValue, actualValue);

        double normalizedLevenshteinDistance = getNormalizedLevenshteinDistance(previousValue, actualValue);

        return jaroWinklerSimilarityScore > normalizedLevenshteinDistance
                ? jaroWinklerSimilarityScore : normalizedLevenshteinDistance;
    }

    private double getNormalizedLevenshteinDistance(final String previousValue, final String actualValue) {
        int distance = levenshteinDistance.apply(previousValue, actualValue);
        int maxLength = Math.max(previousValue.length(), actualValue.length());
        return 1.0 - ((double) distance / maxLength);
    }

    private HealedLocators generateXpathAndCssSelector(final WebElement healedElement) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script =
                "var el = arguments[0];" +
                "var results = { xpaths: [], cssSelectors: [] };" +

                "var attrs = ['name', 'placeholder', 'type', 'role', 'aria-label', 'data-testid'];" +
                "attrs.forEach(attr => {" +
                "  if(el.getAttribute(attr)) {" +
                "    results.xpaths.push('//' + el.tagName.toLowerCase() + '[@' + attr + '=\"' + el.getAttribute(attr) + '\"]');" +
                "  }" +
                "});" +

                "function getRelativeXPath(node) {" +
                "  var parts = [];" +
                "  while (node && node.nodeType === Node.ELEMENT_NODE) {" +
                "    if (node.id) {" +
                "      parts.unshift('//*[@id=\"' + node.id + '\"]');" +
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

                "if (el.className) {" +
                "  var classes = el.className.trim().split(/\\s+/).join('.');" +
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

    private static class ScoredElement {
        @Getter
        WebElement element;
        double score;

        ScoredElement(final WebElement element, final double score) {
            this.element = element;
            this.score = score;
        }
    }
}
