package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.Api;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import com.knubisoft.testlum.testing.model.scenario.Logout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthCommandExpander {

    private final IntegrationsUtil integrationUtil;
    private final Integrations integrations;

    public List<AbstractCommand> expand(final List<AbstractCommand> commands) {
        List<AbstractCommand> expanded = new ArrayList<>();
        for (AbstractCommand command : commands) {
            addCommand(expanded, command);
        }
        return expanded;
    }

    private void addCommand(final List<AbstractCommand> expanded, final AbstractCommand command) {
        if (command instanceof Auth auth) {
            addAuthCommands(expanded, auth);
        } else {
            expanded.add(command);
        }
    }

    private void addAuthCommands(final List<AbstractCommand> expanded, final Auth authCommand) {
        Auth auth = new Auth();
        auth.setComment(authCommand.getComment());
        auth.setCredentials(authCommand.getCredentials());
        auth.setApiAlias(authCommand.getApiAlias());
        auth.setLoginEndpoint(authCommand.getLoginEndpoint());
        expanded.add(auth);
        expanded.addAll(authCommand.getCommands());
        if (isAutoLogout(authCommand.getApiAlias())) {
            Logout logout = new Logout();
            logout.setAlias(authCommand.getApiAlias());
            expanded.add(logout);
        }
    }

    private boolean isAutoLogout(final String alias) {
        //todo move to interpreter
        List<Api> apiList = integrations.getApis().getApi();
        Api apiIntegration = integrationUtil.findApiForAlias(apiList, alias);
        if (Objects.nonNull(apiIntegration.getAuth())) {
            return apiIntegration.getAuth().isAutoLogout();
        }
        throw new DefaultFrameworkException(ExceptionMessage.AUTH_NOT_FOUND, apiIntegration.getAlias());
    }

}
