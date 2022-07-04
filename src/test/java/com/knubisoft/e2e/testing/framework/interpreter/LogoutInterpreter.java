package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.BasicLogout;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.LogoutStrategy;
import com.knubisoft.e2e.testing.model.global_config.Basic;
import com.knubisoft.e2e.testing.model.scenario.Logout;
import com.knubisoft.e2e.testing.framework.report.CommandResult;

@InterpreterForClass(Logout.class)
public class LogoutInterpreter extends AbstractInterpreter<Logout> {

    public LogoutInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Logout o, final CommandResult result) {
//        dependencies.setAuthorization(null);
//        WebDriver driver = dependencies.getWebDriver();
//        if (driver instanceof WebStorage) {
//            WebStorage webStorage = (WebStorage) driver;
//            webStorage.getLocalStorage().removeItem(
//                    dependencies.getGlobalTestConfiguration().getLocalStorageAuthorizationKey());
//        }
        LogoutStrategy logoutStrategy = null;
        Basic basicAuth = dependencies.getGlobalTestConfiguration().getAuth().getBasic();
        if(basicAuth != null){
            logoutStrategy = new BasicLogout();
        }
        assert logoutStrategy != null;
        logoutStrategy.logout(dependencies);
    }
}
