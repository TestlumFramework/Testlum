package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.auth.AuthFactory;
import com.testlum.testing.framework.auth.AuthStrategy;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.CommandRunner;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Auth;

import java.util.ArrayList;

@InterpreterForClass(Auth.class)
public class AuthInterpreter extends AbstractInterpreter<Auth> {

    private static final String STRATEGY_ALIAS = "Auth strategy alias";
    private static final String CREDENTIALS_FILE = "Credentials file";
    private static final String AUTO_LOGOUT = "Auto logout";

    private final AuthFactory authFactory;
    private final CommandRunner commandRunner;

    public AuthInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.authFactory = dependencies.getContext().getBean(AuthFactory.class);
        this.commandRunner = dependencies.getContext().getBean(CommandRunner.class);
    }

    @Override
    protected void acceptImpl(final Auth command, final CommandResult result) {
        if (dependencies.isAuthBlockActive()) {
            throw new DefaultFrameworkException("Nested auth blocks are not supported");
        }
        dependencies.setAuthBlockActive(true);
        try {
            executeAuthBlock(command, result);
        } finally {
            dependencies.setAuthBlockActive(false);
        }
    }

    private void executeAuthBlock(final Auth command, final CommandResult result) {
        Auth auth = prepareAuthCommand(command);
        addAuthMetaData(auth, result);
        result.setSubCommandsResult(new ArrayList<>());
        dependencies.clearAuthorization();
        AuthStrategy strategy = authFactory.create(dependencies, auth.getStrategyAlias());
        Throwable failure = null;
        try {
            authenticate(strategy, auth, result);
            commandRunner.runCommands(command.getCommands(), dependencies, result, result.getSubCommandsResult());
        } catch (RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            if (auth.isAutoLogout()) {
                logout(strategy, failure);
            }
        }
    }

    private Auth prepareAuthCommand(final Auth command) {
        Auth auth = new Auth();
        auth.setStrategyAlias(inject(command.getStrategyAlias()));
        auth.setCredentials(inject(command.getCredentials()));
        auth.setAutoLogout(command.isAutoLogout());
        ensureAlias(auth::getStrategyAlias, auth::setStrategyAlias);
        return auth;
    }

    private void authenticate(final AuthStrategy strategy,
                              final Auth auth,
                              final CommandResult result) {
        try {
            strategy.authenticate(auth, result);
        } catch (RuntimeException exception) {
            dependencies.clearAuthorization();
            throw exception;
        }
    }

    private void addAuthMetaData(final Auth auth, final CommandResult result) {
        result.put(STRATEGY_ALIAS, auth.getStrategyAlias());
        result.put(CREDENTIALS_FILE, auth.getCredentials());
        result.put(AUTO_LOGOUT, auth.isAutoLogout());
    }

    private void logout(final AuthStrategy strategy, final Throwable failure) {
        try {
            strategy.logout();
        } catch (RuntimeException | Error exception) {
            if (failure == null) {
                throw exception;
            }
            if (failure != exception) {
                failure.addSuppressed(exception);
            }
        } finally {
            dependencies.clearAuthorization();
        }
    }
}