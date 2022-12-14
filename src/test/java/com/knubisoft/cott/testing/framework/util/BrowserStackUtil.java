package com.knubisoft.cott.testing.framework.util;

import com.browserstack.local.Local;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;

import java.util.HashMap;

import static com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider.getBrowserStack;

@UtilityClass
public class BrowserStackUtil {
    private static final Local BS_LOCAL = new Local();
    private static final HashMap<String, String> BS_LOCAL_ARGS = new HashMap<>();

    public void startLocalServer() {
        BS_LOCAL_ARGS.put("key", getBrowserStack().getBrowserStackLogin().getPassword());
        try {
            BS_LOCAL.start(BS_LOCAL_ARGS);
        } catch (Exception e) {
            throw new DefaultFrameworkException();
        }
    }

    public void closeLocalServer() {
        try {
            BS_LOCAL.stop();
        } catch (Exception e) {
            throw new DefaultFrameworkException();
        }
    }
}
