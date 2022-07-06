package com.knubisoft.e2e.testing.framework.configuration.auth;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnAuthEnabledCondition;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.BasicAuth;
import com.knubisoft.e2e.testing.model.global_config.Auth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional({OnAuthEnabledCondition.class})
public class AuthConfiguration {
    @Bean
    AuthStrategy getAuthStrategy() {
        final Auth auth = GlobalTestConfigurationProvider.provide().getAuth();
        if (auth.getBasic() != null) {
            return new BasicAuth();
        }
        return null;
    }

}
