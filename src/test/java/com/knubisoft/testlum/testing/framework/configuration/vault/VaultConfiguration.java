package com.knubisoft.testlum.testing.framework.configuration.vault;

import com.bettercloud.vault.VaultConfig;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnVaultEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Vault;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional(OnVaultEnabledCondition.class)
public class VaultConfiguration {

    @Bean
    public Map<AliasEnv, com.bettercloud.vault.Vault> vault() {
        Map<AliasEnv, com.bettercloud.vault.Vault> vaultMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> addVaultToMap(integrations, env, vaultMap));
        return vaultMap;
    }

    private void addVaultToMap(final Integrations integrations,
                               final String env,
                               final Map<AliasEnv, com.bettercloud.vault.Vault> vaultMap) {
        for (Vault vault : integrations.getVaultIntegration().getVault()) {
            if (vault.isEnabled()) {
                vaultMap.put(new AliasEnv(vault.getAlias(), env),
                        new com.bettercloud.vault.Vault(createVaultConfig(vault)));
            }
        }
    }

    @SneakyThrows
    private VaultConfig createVaultConfig(final Vault vault) {
        return new VaultConfig()
                .address(vault.getAddress())
                .token(vault.getToken())
                .build();
    }
}
