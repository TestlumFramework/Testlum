package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.e2e.testing.model.global_config.Auth;
import com.knubisoft.e2e.testing.model.global_config.ClearLocalStorage;
import com.knubisoft.e2e.testing.model.scenario.Logout;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.WebStorage;
import org.springframework.beans.factory.annotation.Autowired;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLEAR_COOKIES;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLEAR_LOCAL_STORAGE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.DRIVER_HAS_NOT_BEEN_INITIALIZED;
@Slf4j
@InterpreterForClass(Logout.class)
public class LogoutInterpreter extends AbstractInterpreter<Logout> {

    @Autowired
    private AuthStrategy authStrategy;

    public LogoutInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Logout o, final CommandResult result) {
        authStrategy.logout(dependencies);
        Auth auth = dependencies.getGlobalTestConfiguration().getAuth();
        ClearLocalStorage clearStorage = auth.getClearLocalStorage();
        boolean cookies = auth.isClearCookies();
        WebDriver driver = dependencies.getWebDriver();
        if ((auth.isClearCookies() || clearStorage != null) && driver == null) {
            throw new DefaultFrameworkException(DRIVER_HAS_NOT_BEEN_INITIALIZED);
        }
        clearLocalStorage(driver, clearStorage);
        clearCookies(driver, cookies);

    }

    private void clearLocalStorage(final WebDriver driver, final ClearLocalStorage clearLocalStorage) {
        if (clearLocalStorage != null && clearLocalStorage.isEnabled()) {
            log.info(CLEAR_LOCAL_STORAGE, clearLocalStorage.getLocalStorageKey());
            WebStorage webStorage = (WebStorage) driver;
            webStorage.getLocalStorage().removeItem(clearLocalStorage.getLocalStorageKey());
        }

    }

    private void clearCookies(final WebDriver driver, final boolean clearCookies) {
        log.info(CLEAR_COOKIES, clearCookies);
        if (clearCookies) {
            driver.manage().deleteAllCookies();
        }
    }
}
