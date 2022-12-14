package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.WebView;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.SupportsContextSwitching;

import java.util.Set;

@ExecutorForClass(WebView.class)
public class WebViewExecutor extends AbstractUiExecutor<WebView> {

    public WebViewExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final WebView webView, final CommandResult result) {
        AndroidDriver androidDriver = (AndroidDriver) dependencies.getDriver();
        SupportsContextSwitching driver = (SupportsContextSwitching) dependencies.getDriver();
        Set<String> contextNames = driver.getContextHandles();
        for (String contextName : contextNames) {
            if (contextName.toLowerCase().contains("web")) {
                driver.context(contextName);
                return;
            }
        }
    }
}
