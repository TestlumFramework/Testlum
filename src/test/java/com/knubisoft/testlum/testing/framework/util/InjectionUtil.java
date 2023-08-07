package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.framework.vaultService.VaultService;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Map;

@UtilityClass
public class InjectionUtil {

    private static final String ROUTE_REGEXP = "\\$\\{(.*?)}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);


    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = scenarioContext.inject(asJson);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SuppressWarnings("unchecked")
    public <T> T injectObjectVariation(final T t, final Map<String, String> variation) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(t);
        String injected = GlobalVariations.getVariationValue(asJson, variation);
        return JacksonMapperUtil.readCopiedValue(injected, (Class<T>) t.getClass());
    }

    @SneakyThrows
    public Integrations injectFromVault(final Integrations integrations,
                                        final Map<String, List<VaultService.VaultDto>> vaultByPath) {
        String asJson = JacksonMapperUtil.writeValueToCopiedString(integrations);
        String injected = inject(asJson, vaultByPath);
        return JacksonMapperUtil.readCopiedValue(injected, integrations.getClass());
    }

    public String inject(final String toInject, final Map<String, List<VaultService.VaultDto>> vaultByPath) {
        if (StringUtils.isBlank(toInject)) {
            return toInject;
        }
        Matcher m = ROUTE_PATTERN.matcher(toInject);
        return getFormattedInject(toInject, m, vaultByPath);
    }

    private String getFormattedInject(final String original, final Matcher m,
                                      final Map<String, List<VaultService.VaultDto>> vaultByPath) {
        String formatted = original;
        while (m.find()) {
            String vaultKey = m.group(1);
            String vaultKeyInBraces = m.group(0);
            String vaultValue = getValue(vaultKey, vaultByPath);
            vaultValue = StringEscapeUtils.escapeJson(vaultValue);
            formatted = formatted.replace(vaultKeyInBraces, vaultValue);
        }
        return formatted;
    }

    private String getValue(final String vaultKey, final Map<String, List<VaultService.VaultDto>> vaultByPath) {
        String[] divided = vaultKey.split("\\.");
        String path = divided[0];
        Map.Entry<String, List<VaultService.VaultDto>> data = vaultByPath.entrySet().stream()
                .filter(e -> e.getKey().contains(path))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException("No such path in vault"));
        List<VaultService.VaultDto> vaultData = data.getValue();
        return vaultData.stream()
                .filter(vaultDto -> vaultKey.equals(vaultDto.getKey()))
                .map(VaultService.VaultDto::getValue)
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException("No such key in Vault"));
    }
}
