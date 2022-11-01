package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.SingleKeyActionEnum;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static java.lang.String.format;

@UtilityClass
public class SingleKeyCommandUtil {

    private static final Map<SingleKeyActionValue, Keys> SINGLE_KEYS;

    static {
        final Map<SingleKeyActionValue, Keys> map = new HashMap<>();
        map.put(act -> SingleKeyActionEnum.TAB.value().equals(act.value()), Keys.TAB);
        map.put(act -> SingleKeyActionEnum.ENTER.value().equals(act.value()), Keys.ENTER);
        map.put(act -> SingleKeyActionEnum.ESCAPE.value().equals(act.value()), Keys.ESCAPE);
        map.put(act -> SingleKeyActionEnum.DELETE.value().equals(act.value()), Keys.BACK_SPACE);
        map.put(act -> SingleKeyActionEnum.SPACE.value().equals(act.value()), Keys.SPACE);
        map.put(act -> SingleKeyActionEnum.ARROW_LEFT.value().equals(act.value()), Keys.ARROW_LEFT);
        map.put(act -> SingleKeyActionEnum.ARROW_RIGHT.value().equals(act.value()), Keys.ARROW_RIGHT);
        map.put(act -> SingleKeyActionEnum.ARROW_DOWN.value().equals(act.value()), Keys.ARROW_DOWN);
        map.put(act -> SingleKeyActionEnum.ARROW_UP.value().equals(act.value()), Keys.ARROW_UP);
        SINGLE_KEYS = Collections.unmodifiableMap(map);
    }

    private Keys findKeysByEnumValue(final SingleKeyActionEnum singleKeyActionEnum) {
        return SINGLE_KEYS.entrySet().stream()
                .filter(act -> act.getKey().test(singleKeyActionEnum))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() ->
                        new DefaultFrameworkException(format(ExceptionMessage.KEY_NOT_SUPPORTED, singleKeyActionEnum)));
    }

    public void singleKeyCommand(final SingleKeyActionEnum singleKeyActionEnum, final WebDriver driver) {
        Actions action = new Actions(driver);
        action.sendKeys(findKeysByEnumValue(singleKeyActionEnum)).perform();
    }

    private interface SingleKeyActionValue extends Predicate<SingleKeyActionEnum> { }

}
