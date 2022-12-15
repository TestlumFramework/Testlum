package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.WebView;
import io.appium.java_client.remote.SupportsContextSwitching;

import java.util.Locale;
import java.util.Set;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.CANNOT_SWITCH_TO_WEBVIEW;

@ExecutorForClass(WebView.class)
public class WebViewExecutor extends AbstractUiExecutor<WebView> {

    public WebViewExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final WebView webView, final CommandResult result) {
        SupportsContextSwitching driver = (SupportsContextSwitching) dependencies.getDriver();
        Set<String> contextNames = driver.getContextHandles();
        try {
            driver.context(contextNames.stream()
                    .filter(context -> context.toLowerCase(Locale.US).contains("web"))
                    .findFirst()
                    .orElseThrow(DefaultFrameworkException::new));
        } catch (Exception exception) {
            throw new DefaultFrameworkException(CANNOT_SWITCH_TO_WEBVIEW);
        }

    }
}
