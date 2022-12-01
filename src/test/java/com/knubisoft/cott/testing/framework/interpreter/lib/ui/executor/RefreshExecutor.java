package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Refresh;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import java.time.Duration;
import java.util.Collections;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@ExecutorForClass(Refresh.class)
public class RefreshExecutor extends AbstractUiExecutor<Refresh> {

    private final AppiumDriver driver;

    public RefreshExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = (AppiumDriver) dependencies.getDriver();
    }

    @Override
    public void execute(final Refresh refresh, final CommandResult result) {
        Dimension dimensions = driver.manage().window().getSize();
        Point start = new Point(dimensions.width/2, dimensions.height/5);
        driver.perform(Collections.singletonList(buildSequence(start, new Point(start.x, -1))));
        driver.perform(Collections.singletonList(buildSequence(start, new Point(start.x, 1500))));

    }
    public Sequence buildSequence(final Point start, final Point end) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        return new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), viewport(), start.x, start.y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(300), viewport(), end.x, end.y))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    }
}
