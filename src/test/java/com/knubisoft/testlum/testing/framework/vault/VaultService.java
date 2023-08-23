package com.knubisoft.testlum.testing.framework.vault;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.vault.model.VaultDto;
import com.knubisoft.testlum.testing.model.global_config.Vault;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class VaultService {

    private static final String VAULT_KEY = "data";
    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    private static final VaultTemplate TEMPLATE = vault();

    public VaultTemplate vault() {
        if (Objects.nonNull(GlobalTestConfigurationProvider.provide().getVault())) {
            Vault vault = GlobalTestConfigurationProvider.provide().getVault();
            return new VaultTemplate(vaultEndpoint(vault), new TokenAuthentication(vault.getToken()));
        } else {
            throw new DefaultFrameworkException("Vault is not enabled in global config file");
        }
    }

    private VaultEndpoint vaultEndpoint(final Vault vault) {

        VaultEndpoint endpoint = new VaultEndpoint();
        endpoint.setHost(vault.getHost());
        endpoint.setPort(vault.getPort());
        endpoint.setScheme(vault.getScheme());
        return endpoint;
    }

    private List<VaultDto> objToVaultDto(final List<Object> vaultList) {
        return vaultList.stream()
                .map(JacksonMapperUtil::writeValueAsString)
                .map(s -> JacksonMapperUtil.readVaultValue(s, VaultDto.class))
                .collect(Collectors.toList());
    }

    private List<VaultDto> getVaultByPath(final Map<String, Object> dataByPath) {
        List<Object> vaultList = Objects.requireNonNull(dataByPath).entrySet().stream()
                .filter(entry -> VAULT_KEY.equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        return objToVaultDto(vaultList);
    }

    public String inject(final String toInject) {
        if (StringUtils.isBlank(toInject)) {
            return toInject;
        }
        Matcher m = ROUTE_PATTERN.matcher(toInject);
        return getFormattedInject(toInject, m);
    }

    private String getFormattedInject(final String original, final Matcher m) {
        String formatted = original;
        while (m.find()) {
            String vaultKey = m.group(1);
            String vaultKeyInBraces = m.group(0);
            String[] divided = vaultKey.split("\\.");
            String path = divided[0];
            String key = divided[1];
            String vaultValue = getValue(vaultKey, path, key);
            vaultValue = StringEscapeUtils.escapeJson(vaultValue);
            formatted = formatted.replace(vaultKeyInBraces, vaultValue);
        }
        return formatted;
    }

    private String getValue(final String vaultKey, final String path, final String key) {
        if (Objects.nonNull(TEMPLATE.read(path).getData())) {
            Map<String, Object> data = Objects.requireNonNull(TEMPLATE.read(path)).getData();
            List<VaultDto> convertedVault = getVaultByPath(data);
            return convertedVault.stream()
                    .filter(vaultDto -> key.equals(vaultDto.getKey()))
                    .map(VaultDto::getValue)
                    .findFirst()
                    .orElseThrow(() -> new DefaultFrameworkException("No such key in Vault: %s", key));
        } else {
            throw new DefaultFrameworkException("No data found for path: %s", vaultKey);
        }
    }
}
