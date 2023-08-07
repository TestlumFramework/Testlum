package com.knubisoft.testlum.testing.framework.vaultService;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Vault;
import lombok.Data;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class VaultService {

    private static final String VAULT_KEY = "data";
    private static final String VAULT_HOST = "127.0.0.1";
    private static final int VAULT_PORT = 8200;
    private static final String VAULT_SCHEME = "http";

    private final VaultTemplate template;

    public VaultService() {
        this.template = vault();
    }

    public Integrations getWithVault(final Integrations integrations) {
        List<String> paths = getVaultForAllPaths();
        Map<String, List<VaultDto>> vaultByPath = new HashMap<>();
        for (String path : paths) {
            Map<String, Object> data = Objects.requireNonNull(template.read(path)).getData();
            List<Object> vaultList = Objects.requireNonNull(data).entrySet().stream()
                    .filter(entry -> VAULT_KEY.equals(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            List<VaultDto> convertedVault = objToVaultDto(vaultList);
            vaultByPath.put(path, convertedVault);
        }
        return InjectionUtil.injectFromVault(integrations, vaultByPath);
    }

    public VaultTemplate vault() {
        Vault vault = GlobalTestConfigurationProvider.provide().getVault();
        if (vault.isEnabled()) {
            return new VaultTemplate(vaultEndpoint(), new TokenAuthentication(vault.getToken()));
        } else {
            throw new DefaultFrameworkException("Vault is not enabled in global config file");
        }
    }

    public VaultEndpoint vaultEndpoint() {

        VaultEndpoint endpoint = new VaultEndpoint();
        endpoint.setHost(VAULT_HOST);
        endpoint.setPort(VAULT_PORT);
        endpoint.setScheme(VAULT_SCHEME);
        return endpoint;
    }

    private List<VaultDto> objToVaultDto(final List<Object> vaultList) {
        List<String> formattedObjects = vaultList.stream()
                .map(JacksonMapperUtil::writeValueAsString)
                .collect(Collectors.toList());
        return formattedObjects.stream()
                .map(s -> JacksonMapperUtil.readVaultValue(s, VaultDto.class))
                .collect(Collectors.toList());
    }

    private List<String> getVaultForAllPaths() {
        Vault vault = GlobalTestConfigurationProvider.provide().getVault();
        return vault.getPath();
    }

    @Data
    public static class VaultDto {
        private final String key;
        private final String value;

    }

    public static class VaultDtoDeserializer extends StdDeserializer<VaultDto> {

        public VaultDtoDeserializer() {
            this(null);
        }

        public VaultDtoDeserializer(final Class<?> vc) {
            super(vc);
        }

        @Override
        public VaultDto deserialize(final JsonParser jsonParser,
                                    final DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            String key = node.fieldNames().next();
            String value = node.get(key).asText();

            return new VaultDto(key, value);
        }
    }

}
