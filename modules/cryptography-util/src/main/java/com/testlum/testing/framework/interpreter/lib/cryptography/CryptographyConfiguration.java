package com.testlum.testing.framework.interpreter.lib.cryptography;

import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.model.global_config.Cryptography;
import com.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional(OnCryptographyEnabledCondition.class)
@RequiredArgsConstructor
public class CryptographyConfiguration {

    /**
     * Creates and registers a map of alias/environment pairs to Cryptography instances.
     *
     * @param envToIntegrations map of environment names to parsed integrations
     * @return map of alias and environment pairs to configured Cryptography beans
     */
    @Bean("cryptographyIntegrations")
    public Map<AliasEnv, Cryptography> cryptographyIntegrations(
            final EnvToIntegrationMap envToIntegrations) {
        final Map<AliasEnv, Cryptography> cryptographyMap = new HashMap<>();
        envToIntegrations.forEach((env, integrations) ->
                this.addCryptographyToMap(integrations, env, cryptographyMap));
        return cryptographyMap;
    }

    private void addCryptographyToMap(final Integrations integrations,
                                      final String env,
                                      final Map<AliasEnv, Cryptography> cryptographyMap) {
        if (integrations.getCryptographyIntegrations() == null) {
            return;
        }

        for (final Cryptography crypto : integrations.getCryptographyIntegrations().getCryptography()) {
            if (crypto.isEnabled()) {
                cryptographyMap.put(new AliasEnv(crypto.getAlias(), env), crypto);
            }
        }
    }

}
