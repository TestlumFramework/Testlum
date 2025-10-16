package com.knubisoft.testlum.testing.framework.configuration.googleauth;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnGoogleAuthEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.GoogleAuth;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnGoogleAuthEnabledCondition.class})
public class GoogleAuthConfiguration {

	@Bean("googleAuth")
	public Map<AliasEnv, GoogleAuth> googleAuth() {
		Map<AliasEnv, GoogleAuth> googleAuthMap = new HashMap<>();
		ConfigProviderImpl.GlobalTestConfigurationProvider.getIntegrations()
				.forEach((env, integrations) -> addToMap(integrations, env, googleAuthMap));
		return googleAuthMap;
	}

	public void addToMap(final Integrations integrations,
	                     final String env,
	                     final Map<AliasEnv, GoogleAuth> googleAuthMap) {
		for (GoogleAuth googleAuth : integrations.getGoogleAuthIntegration().getGoogleAuth()) {
			if (googleAuth.isEnabled()) {
				googleAuthMap.put(new AliasEnv(googleAuth.getAlias(), env), googleAuth);
			}
		}
	}
}
