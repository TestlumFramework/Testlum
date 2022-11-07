package com.knubisoft.cott.testing.framework.locator;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.parser.XMLParsers;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.model.pages.Component;
import com.knubisoft.cott.testing.model.pages.Include;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.pages.Page;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.UNABLE_PARSE_FILE_WITH_LOCATORS;
import static java.lang.String.format;

public class LocatorCollector {

    private final File pagesFolder;
    private final File componentsFolder;

    public LocatorCollector() {
        TestResourceSettings resourceSettings = TestResourceSettings.getInstance();
        this.pagesFolder = resourceSettings.getPagesFolder();
        this.componentsFolder = resourceSettings.getComponentsFolder();
    }

    public Map<String, Locator> collect() {
        Map<File, Page> fileToPage = collectFileToPageMap();
        return transformToNameToLocatorMap(fileToPage);
    }

    private Map<File, Page> collectFileToPageMap() {
        Map<File, Page> fileToPage = new LinkedHashMap<>();
        File[] listFiles = Objects.requireNonNull(pagesFolder.listFiles());
        for (File each : listFiles) {
            fileToPage.put(each, parseLocatorOrThrow(each));
        }
        return fileToPage;
    }

    private Page parseLocatorOrThrow(final File each) {
        try {
            PageValidator pageValidator = new PageValidator();
            return XMLParsers.forPageLocator().process(each, pageValidator);
        } catch (Exception e) {
            throw new DefaultFrameworkException(
                    format(UNABLE_PARSE_FILE_WITH_LOCATORS, each.getName(), e.getMessage()), e);
        }
    }

    @NotNull
    private Map<String, Locator> transformToNameToLocatorMap(final Map<File, Page> fileToPage) {
        Map<String, Locator> result = new LinkedHashMap<>();

        for (Map.Entry<File, Page> each : fileToPage.entrySet()) {
            for (Locator locator : getLocators(each)) {
                result.put(getKeyName(each, locator), locator);
            }
        }
        return result;
    }

    private List<Locator> getLocators(Map.Entry<File, Page> each) {
        List<Locator> result = each.getValue().getLocators().getLocator();

        for (Include include : each.getValue().getInclude()) {
            Component component = parseComponent(include);
            List<Locator> locatorsFromComponent = component.getLocators().getLocator();
            validateLocators(each.getKey(), result, locatorsFromComponent, include);
            result.addAll(locatorsFromComponent);
        }
        return result;

    }

    private void validateLocators(File file, List<Locator> result, List<Locator> locatorsFromComponent, Include include) {
        Set<String> ids = result.stream()
                .map(Locator::getLocatorId)
                .collect(Collectors.toSet());

        for (Locator each : locatorsFromComponent) {
            String locatorId = each.getLocatorId();
            if (ids.contains(locatorId)) {
                throw new DefaultFrameworkException(
                        "Locator with id <%s> from component <%s> is duplicated in <%s> page",
                         locatorId, include.getComponent(), file.getName());
            }
            ids.add(locatorId);
        }

    }


    private Component parseComponent(final Include include) {
        File file = FileSearcher.searchFileFromDir(componentsFolder, include.getComponent());
        return XMLParsers.forComponentLocator().process(file);
    }

    private String getKeyName(final Map.Entry<File, Page> each, final Locator locator) {
        String prefix = each.getKey().getName().replace(TestResourceSettings.XML_SUFFIX, DelimiterConstant.EMPTY)
                + DelimiterConstant.DOT;
        return prefix + locator.getLocatorId();
    }
}
