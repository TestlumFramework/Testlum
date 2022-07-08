package com.knubisoft.e2e.testing.framework.configuration.auth;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.BasicAuth;
import com.knubisoft.e2e.testing.model.global_config.Auth;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

@UtilityClass
public class AuthFactory {
    public AuthStrategy create() {
        final Auth auth = GlobalTestConfigurationProvider.provide().getAuth();
        switch (auth.getAuthStrategy()) {
            case BASIC:
                return new BasicAuth();
            case JWT:
                throw new NotImplementedException();
            case OAUTH_2:
                throw new NotImplementedException();
            case CUSTOM:
                try{
                    return (AuthStrategy) Class.forName(auth.getAuthCustomClassName()).newInstance();
                } catch (Exception e) {
                    throw new NotImplementedException();
                }
            default:
                throw new UnsupportedOperationException();
        }
    }
}
