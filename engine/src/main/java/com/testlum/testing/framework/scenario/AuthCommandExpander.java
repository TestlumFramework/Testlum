package com.testlum.testing.framework.scenario;

import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Auth;
import com.testlum.testing.model.scenario.Logout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthCommandExpander {

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

    private void addAuthCommands(final List<AbstractCommand> expanded,
                                 final Auth authCommand) {
        expanded.add(copyAuthCommand(authCommand));
        expanded.addAll(authCommand.getCommands());
        if (authCommand.isAutoLogout()) {
            Logout logout = new Logout();
            logout.setAlias(authCommand.getStrategyAlias());
            logout.setComment("Clear authorization after auth block");
            expanded.add(logout);
        }
    }

    private Auth copyAuthCommand(final Auth source) {
        Auth auth = new Auth();
        auth.setComment(source.getComment());
        auth.setCondition(source.getCondition());
        auth.setThreshold(source.getThreshold());
        auth.setCredentials(source.getCredentials());
        auth.setStrategyAlias(source.getStrategyAlias());
        auth.setAutoLogout(source.isAutoLogout());
        return auth;
    }
}
