package com.knubisoft.testlum.testing.framework.vaultService;

import com.bettercloud.vault.Vault;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultService {

    private static final String ROUTE_REGEXP = "\\$\\{(.*?)}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    @Autowired(required = false)
    private Map<AliasEnv, Vault> vaultMap;

    public static Integrations getWithVault(final Integrations integrations) {

    }

    public String injectFromVault(final String toInject, final Map<String, String> vaultData) {
        if (StringUtils.isBlank(toInject)) {
            return toInject;
        }
        Matcher m = ROUTE_PATTERN.matcher(toInject);
        return getFormattedInject(toInject, m, vaultData);
    }

    private String getFormattedInject(final String original, final Matcher m, final Map<String, String> vaultData) {
        String formatted = original;
        while (m.find()) {
            String vaultKey = m.group(1);
            String vaultKeyInBraces = m.group(0);
            String vaultValue = vaultData.get(vaultKey);
            vaultValue = StringEscapeUtils.escapeJson(vaultValue);
            formatted = formatted.replace(vaultKeyInBraces, vaultValue);
        }
        return formatted;
    }
}
