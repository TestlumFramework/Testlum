package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl;
import com.knubisoft.testlum.testing.model.global_config.GoogleAuthIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnGoogleAuthEnabledCondition implements Condition {

	private final GoogleAuthIntegration googleAuthIntegration =
			ConfigProviderImpl.GlobalTestConfigurationProvider.getDefaultIntegrations().getGoogleAuthIntegration();
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		if (Objects.nonNull(googleAuthIntegration)) {
			return IntegrationsProviderImpl.IntegrationsUtil.isEnabled(googleAuthIntegration.getGoogleAuth());
		}
		return false;
	}
}
