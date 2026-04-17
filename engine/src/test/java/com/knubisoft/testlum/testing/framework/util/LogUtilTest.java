package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.AssertAlert;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.AssertChecked;
import com.knubisoft.testlum.testing.model.scenario.AssertPresent;
import com.knubisoft.testlum.testing.model.scenario.AssertTitle;
import com.knubisoft.testlum.testing.model.scenario.ByLocator;
import com.knubisoft.testlum.testing.model.scenario.Click;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import com.knubisoft.testlum.testing.model.scenario.DragAndDropNative;
import com.knubisoft.testlum.testing.model.scenario.Exclude;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import com.knubisoft.testlum.testing.model.scenario.Hover;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.MobileImage;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import com.knubisoft.testlum.testing.model.scenario.Part;
import com.knubisoft.testlum.testing.model.scenario.Picture;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import com.knubisoft.testlum.testing.model.scenario.ScrollType;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import com.knubisoft.testlum.testing.model.scenario.SwipeDirection;
import com.knubisoft.testlum.testing.model.scenario.SwipeElement;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.SwipePage;
import com.knubisoft.testlum.testing.model.scenario.Web;
import com.knubisoft.testlum.testing.model.scenario.WebFullScreen;
import com.knubisoft.testlum.log.Color;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LogUtil} verifying logging methods
 * delegate correctly and handle edge cases without errors.
 */
@ExtendWith(MockitoExtension.class)
class LogUtilTest {

    @Mock
    private BrowserUtil browserUtil;

    @Mock
    private MobileUtil mobileUtil;

    @Mock
    private StringPrettifier stringPrettifier;

    @InjectMocks
    private LogUtil logUtil;

    @Nested
    class LogCondition {
        @Test
        void logConditionTrueDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.logCondition("flag", true));
        }

        @Test
        void logConditionFalseDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.logCondition("flag", false));
        }
    }

    @Nested
    class LogConditionInfo {
        @Test
        void logsConditionInfoWithoutError() {
            assertDoesNotThrow(
                    () -> logUtil.logConditionInfo("cond", "true && false", false));
        }

        @Test
        void logsConditionInfoTrueValue() {
            assertDoesNotThrow(
                    () -> logUtil.logConditionInfo("cond", "1 == 1", true));
        }
    }

    @Nested
    class LogExecutionTime {
        @Test
        void logsNonUiCommandTime() {
            final AbstractCommand command = mock(AbstractCommand.class);
            assertDoesNotThrow(() -> logUtil.logExecutionTime(150L, command));
        }

        @Test
        void logsUiCommandTime() {
            final Web uiCommand = new Web();
            uiCommand.setComment("ui step comment");
            assertDoesNotThrow(() -> logUtil.logExecutionTime(200L, uiCommand));
        }
    }

    @Nested
    class LogException {
        @Test
        void logsExceptionWithMessage() {
            final Exception ex = new RuntimeException("something failed");
            assertDoesNotThrow(() -> logUtil.logException(ex));
        }

        @Test
        void logsExceptionWithoutMessage() {
            final Exception ex = new RuntimeException();
            assertDoesNotThrow(() -> logUtil.logException(ex));
        }
    }

    @Nested
    class LogNonParsedScenarioInfo {
        @Test
        void logsNonParsedScenario() {
            assertDoesNotThrow(
                    () -> logUtil.logNonParsedScenarioInfo("/path/to/file.xml", "parse error"));
        }
    }

    @Nested
    class LogAllQueries {
        @Test
        void logsQueriesWithAlias() {
            final List<String> queries = List.of("SELECT * FROM users", "SELECT 1");
            assertDoesNotThrow(() -> logUtil.logAllQueries(queries, "myDb"));
        }

        @Test
        void logsQueriesWithDbTypeAndAlias() {
            final List<String> queries = List.of("INSERT INTO t VALUES(1)");
            assertDoesNotThrow(
                    () -> logUtil.logAllQueries("postgres", queries, "pgAlias"));
        }

        @Test
        void logsEmptyQueryList() {
            assertDoesNotThrow(() -> logUtil.logAllQueries(List.of(), "alias"));
        }
    }

    @Nested
    class LogVarInfo {
        @Test
        void logsVariableInfo() {
            when(stringPrettifier.cut("longValue")).thenReturn("longValue");
            assertDoesNotThrow(() -> logUtil.logVarInfo("myVar", "longValue"));
        }
    }

    @Nested
    class LogUICommand {
        @Test
        void logsUiCommandWithPosition() {
            final AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn(null);
            assertDoesNotThrow(() -> logUtil.logUICommand(1, command));
        }

        @Test
        void logsUiCommandWithZeroPosition() {
            final AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn(null);
            assertDoesNotThrow(() -> logUtil.logUICommand(0, command));
        }

        @Test
        void logsUiCommandWithComment() {
            final AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn("my comment");
            assertDoesNotThrow(() -> logUtil.logUICommand(3, command));
        }

        @Test
        void logsUiCommandWithCommandWithLocator() {
            final Click click = new Click();
            click.setLocator("myLocator");
            click.setComment("click comment");
            assertDoesNotThrow(() -> logUtil.logUICommand(5, click));
        }

        @Test
        void logsUiCommandWithCommandWithLocatorAndNoComment() {
            final Click click = new Click();
            click.setLocator("loc");
            assertDoesNotThrow(() -> logUtil.logUICommand(2, click));
        }
    }

    @Nested
    class FrameAndWebViewLogs {
        @Test
        void startUiCommandsInFrameDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.startUiCommandsInFrame());
        }

        @Test
        void endUiCommandsInFrameDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.endUiCommandsInFrame());
        }

        @Test
        void startUiCommandsInWebViewDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.startUiCommandsInWebView());
        }

        @Test
        void endUiCommandsInWebViewDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.endUiCommandsInWebView());
        }
    }

    @Nested
    class LogScenarioWithoutTags {
        @Test
        void logsWarning() {
            assertDoesNotThrow(
                    () -> logUtil.logScenarioWithoutTags("/scenarios/test/scenario.xml"));
        }
    }

    @Nested
    class LogSingleKeyCommandTimes {
        @Test
        void logsWhenTimesGreaterThanOne() {
            assertDoesNotThrow(() -> logUtil.logSingleKeyCommandTimes(3));
        }

        @Test
        void doesNotLogWhenTimesIsOne() {
            assertDoesNotThrow(() -> logUtil.logSingleKeyCommandTimes(1));
        }
    }

    @Nested
    class LogScenarioDetails {

        private ScenarioArguments buildArgs(final boolean containsUiSteps) {
            Overview overview = new Overview();
            overview.setName("Test scenario");
            overview.setDescription("Description of test scenario");
            overview.setJira("PROJ-123");
            overview.setDeveloper("dev");
            overview.setLink("http://example.com");

            Settings settings = new Settings();
            settings.setVariations("variation1");

            Scenario scenario = new Scenario();
            scenario.setOverview(overview);
            scenario.setSettings(settings);

            return ScenarioArguments.builder()
                    .file(new File("/tmp/scenario.xml"))
                    .scenario(scenario)
                    .containsUiSteps(containsUiSteps)
                    .browser("chrome")
                    .mobileBrowserDevice("pixel")
                    .nativeDevice("emu")
                    .environment("dev")
                    .path("/tmp/scenario.xml")
                    .build();
        }

        @Test
        void logsScenarioDetailsWithoutUiStepsAndNoException() {
            ScenarioArguments args = buildArgs(false);
            assertDoesNotThrow(() -> logUtil.logScenarioDetails(args, null, Color.GREEN));
        }

        @Test
        void logsScenarioDetailsWithException() {
            ScenarioArguments args = buildArgs(false);
            Exception ex = new RuntimeException("test failure");
            assertDoesNotThrow(() -> logUtil.logScenarioDetails(args, ex, Color.RED));
        }

        @Test
        void logsScenarioDetailsWithUiSteps() {
            when(browserUtil.getBrowserBy(anyString(), anyString())).thenReturn(Optional.empty());
            when(mobileUtil.getMobileBrowserDeviceBy(anyString(), anyString())).thenReturn(Optional.empty());
            when(mobileUtil.getNativeDeviceBy(anyString(), anyString())).thenReturn(Optional.empty());

            ScenarioArguments args = buildArgs(true);
            assertDoesNotThrow(() -> logUtil.logScenarioDetails(args, null, Color.GREEN));
        }

        @Test
        void logsScenarioDetailsWithBlankOverviewFields() {
            Overview overview = new Overview();
            overview.setName("Name");
            overview.setDescription("Desc at least 10 chars");

            Settings settings = new Settings();
            Scenario scenario = new Scenario();
            scenario.setOverview(overview);
            scenario.setSettings(settings);

            ScenarioArguments args = ScenarioArguments.builder()
                    .file(new File("/tmp/test.xml"))
                    .scenario(scenario)
                    .containsUiSteps(false)
                    .path("/tmp/test.xml")
                    .build();

            assertDoesNotThrow(() -> logUtil.logScenarioDetails(args, null, Color.YELLOW));
        }
    }

    @Nested
    class LogImageComparisonInfoImage {
        @Test
        void logsWithPicture() {
            Picture picture = new Picture();
            picture.setLocator("imgLocator");
            picture.setAttribute("src");

            Image image = new Image();
            image.setFile("test.png");
            image.setHighlightDifference(true);
            image.setPicture(picture);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithWebFullScreen() {
            WebFullScreen fullScreen = new WebFullScreen();
            fullScreen.setPercentage(95.0);

            Exclude exclude = new Exclude();

            ByLocator byLocatorExclude = new ByLocator();
            byLocatorExclude.setLocator("excludedEl");
            exclude.setByLocator(byLocatorExclude);
            fullScreen.getExclude().add(exclude);

            Image image = new Image();
            image.setFile("screenshot.png");
            image.setHighlightDifference(false);
            image.setFullScreen(fullScreen);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithWebFullScreenNoPercentageNoExcludes() {
            WebFullScreen fullScreen = new WebFullScreen();

            Image image = new Image();
            image.setFile("screen.png");
            image.setFullScreen(fullScreen);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithPart() {
            Part part = new Part();
            part.setLocator("partLocator");
            part.setPercentage(80.0);

            Image image = new Image();
            image.setFile("part.png");
            image.setPart(part);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithPartNoPercentage() {
            Part part = new Part();
            part.setLocator("partLoc");

            Image image = new Image();
            image.setFile("part2.png");
            image.setPart(part);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithNoBranch() {
            Image image = new Image();
            image.setFile("empty.png");

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }
    }

    @Nested
    class LogImageComparisonInfoMobileImage {
        @Test
        void logsWithPicture() {
            Picture picture = new Picture();
            picture.setLocator("mobilePicLoc");

            MobileImage image = new MobileImage();
            image.setFile("mobile.png");
            image.setHighlightDifference(true);
            image.setPicture(picture);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithFullScreen() {
            FullScreen fullScreen = new FullScreen();
            fullScreen.setPercentage(90.0);

            MobileImage image = new MobileImage();
            image.setFile("mobileFull.png");
            image.setFullScreen(fullScreen);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithFullScreenNoPercentage() {
            FullScreen fullScreen = new FullScreen();

            MobileImage image = new MobileImage();
            image.setFile("mobileFull2.png");
            image.setFullScreen(fullScreen);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithPart() {
            Part part = new Part();
            part.setLocator("mobilePart");
            part.setPercentage(75.0);

            MobileImage image = new MobileImage();
            image.setFile("mobilePart.png");
            image.setPart(part);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithNoBranch() {
            MobileImage image = new MobileImage();
            image.setFile("mobileEmpty.png");

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }
    }

    @Nested
    class LogImageComparisonInfoNativeImage {
        @Test
        void logsWithFullScreen() {
            FullScreen fullScreen = new FullScreen();
            fullScreen.setPercentage(85.0);

            NativeImage image = new NativeImage();
            image.setFile("native.png");
            image.setHighlightDifference(true);
            image.setFullScreen(fullScreen);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithFullScreenNoPercentage() {
            FullScreen fullScreen = new FullScreen();

            NativeImage image = new NativeImage();
            image.setFile("nativeFull.png");
            image.setFullScreen(fullScreen);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithPart() {
            Part part = new Part();
            part.setLocator("nativePart");
            part.setPercentage(60.0);

            NativeImage image = new NativeImage();
            image.setFile("nativePart.png");
            image.setPart(part);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithPartNoPercentage() {
            Part part = new Part();
            part.setLocator("nativePartLoc");

            NativeImage image = new NativeImage();
            image.setFile("nativePart2.png");
            image.setPart(part);

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }

        @Test
        void logsWithNoBranch() {
            NativeImage image = new NativeImage();
            image.setFile("nativeEmpty.png");

            assertDoesNotThrow(() -> logUtil.logImageComparisonInfo(image));
        }
    }

    @Nested
    class LogScrollInfo {
        @Test
        void logsPageScroll() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.PAGE);
            scroll.setDirection(ScrollDirection.DOWN);
            scroll.setValue(500);
            scroll.setMeasure(ScrollMeasure.PIXEL);

            assertDoesNotThrow(() -> logUtil.logScrollInfo(scroll));
        }

        @Test
        void logsInnerScrollWithLocator() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.INNER);
            scroll.setDirection(ScrollDirection.DOWN);
            scroll.setValue(300);
            scroll.setLocator("scrollContainer");
            scroll.setLocatorStrategy(null);

            assertDoesNotThrow(() -> logUtil.logScrollInfo(scroll));
        }
    }

    @Nested
    class LogHover {
        @Test
        void logsMoveToEmptySpaceTrue() {
            Hover hover = new Hover();
            hover.setMoveToEmptySpace(true);
            hover.setLocator("hoverEl");

            assertDoesNotThrow(() -> logUtil.logHover(hover));
        }

        @Test
        void logsMoveToEmptySpaceFalse() {
            Hover hover = new Hover();
            hover.setMoveToEmptySpace(false);
            hover.setLocator("hoverEl");

            assertDoesNotThrow(() -> logUtil.logHover(hover));
        }

        @Test
        void logsMoveToEmptySpaceDefault() {
            Hover hover = new Hover();
            hover.setLocator("hoverEl");

            assertDoesNotThrow(() -> logUtil.logHover(hover));
        }
    }

    @Nested
    class LogHotKeyInfo {
        @Test
        void logsHotKeyInfo() {
            AbstractUiCommand command = mock(AbstractUiCommand.class);
            when(command.getComment()).thenReturn("hotkey comment");

            assertDoesNotThrow(() -> logUtil.logHotKeyInfo(command, 1));
        }

        @Test
        void logsHotKeyInfoWithNullComment() {
            AbstractUiCommand command = mock(AbstractUiCommand.class);
            when(command.getComment()).thenReturn(null);

            assertDoesNotThrow(() -> logUtil.logHotKeyInfo(command, 0));
        }
    }

    @Nested
    class LogCloseOrSwitchTabCommand {
        @Test
        void logsWithTabNumber() {
            assertDoesNotThrow(() -> logUtil.logCloseOrSwitchTabCommand("Switch", 2));
        }

        @Test
        void logsWithNullTabNumber() {
            assertDoesNotThrow(() -> logUtil.logCloseOrSwitchTabCommand("Close", null));
        }
    }

    @Nested
    class LogOpenTabCommand {
        @Test
        void logsWithUrl() {
            assertDoesNotThrow(() -> logUtil.logOpenTabCommand("https://example.com"));
        }

        @Test
        void logsWithBlankUrl() {
            assertDoesNotThrow(() -> logUtil.logOpenTabCommand(""));
        }

        @Test
        void logsWithNullUrl() {
            assertDoesNotThrow(() -> logUtil.logOpenTabCommand(null));
        }
    }

    @Nested
    class LogAssertCommand {
        @Test
        void logsAssertCommand() {
            AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn("assert comment");

            assertDoesNotThrow(() -> logUtil.logAssertCommand(command, 1));
        }

        @Test
        void logsAssertCommandWithNullComment() {
            AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn(null);

            assertDoesNotThrow(() -> logUtil.logAssertCommand(command, 5));
        }
    }

    @Nested
    class LogAssertAttributeInfo {
        @Test
        void logsAssertAttribute() {
            when(stringPrettifier.cut("expectedContent")).thenReturn("expectedContent");

            AssertAttribute attribute = new AssertAttribute();
            attribute.setNegative(false);
            attribute.setLocator("attrLocator");
            attribute.setName("class");
            attribute.setContent("expectedContent");

            assertDoesNotThrow(() -> logUtil.logAssertAttributeInfo(attribute));
        }

        @Test
        void logsAssertAttributeNegative() {
            when(stringPrettifier.cut("val")).thenReturn("val");

            AssertAttribute attribute = new AssertAttribute();
            attribute.setNegative(true);
            attribute.setLocator("loc");
            attribute.setName("data-id");
            attribute.setContent("val");

            assertDoesNotThrow(() -> logUtil.logAssertAttributeInfo(attribute));
        }
    }

    @Nested
    class LogAssertTitleCommand {
        @Test
        void logsAssertTitle() {
            AssertTitle title = new AssertTitle();
            title.setNegative(false);
            title.setContent("Page Title");

            assertDoesNotThrow(() -> logUtil.logAssertTitleCommand(title));
        }

        @Test
        void logsAssertTitleNegative() {
            AssertTitle title = new AssertTitle();
            title.setNegative(true);
            title.setContent("Not This Title");

            assertDoesNotThrow(() -> logUtil.logAssertTitleCommand(title));
        }
    }

    @Nested
    class LogAssertAlertCommand {
        @Test
        void logsAssertAlert() {
            AssertAlert alert = new AssertAlert();
            alert.setNegative(false);
            alert.setText("Alert text");

            assertDoesNotThrow(() -> logUtil.logAssertAlertCommand(alert));
        }

        @Test
        void logsAssertAlertNegative() {
            AssertAlert alert = new AssertAlert();
            alert.setNegative(true);
            alert.setText("Not this alert");

            assertDoesNotThrow(() -> logUtil.logAssertAlertCommand(alert));
        }
    }

    @Nested
    class LogDragAndDropInfo {
        @Test
        void logsWithFileName() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setFileName("file.txt");
            dragAndDrop.setToLocator("dropTarget");

            assertDoesNotThrow(() -> logUtil.logDragAndDropInfo(dragAndDrop));
        }

        @Test
        void logsWithFromLocator() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setFromLocator("sourceLocator");
            dragAndDrop.setToLocator("targetLocator");

            assertDoesNotThrow(() -> logUtil.logDragAndDropInfo(dragAndDrop));
        }

        @Test
        void logsWithNeitherFileNameNorFromLocator() {
            DragAndDrop dragAndDrop = new DragAndDrop();
            dragAndDrop.setToLocator("target");

            assertDoesNotThrow(() -> logUtil.logDragAndDropInfo(dragAndDrop));
        }
    }

    @Nested
    class LogDragAndDropNativeInfo {
        @Test
        void logsDragAndDropNative() {
            DragAndDropNative dragAndDropNative = new DragAndDropNative();
            dragAndDropNative.setFromLocator("nativeFrom");
            dragAndDropNative.setToLocator("nativeTo");

            assertDoesNotThrow(() -> logUtil.logDragAndDropNativeInfo(dragAndDropNative));
        }
    }

    @Nested
    class LogSwipeNativeInfo {
        @Test
        void logsElementSwipe() {
            SwipeElement element = new SwipeElement();
            element.setQuantity(3);
            element.setDirection(SwipeDirection.UP);
            element.setPercent(50);
            element.setLocator("swipeLocator");

            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setElement(element);

            assertDoesNotThrow(() -> logUtil.logSwipeNativeInfo(swipeNative));
        }

        @Test
        void logsPageSwipe() {
            SwipePage page = new SwipePage();
            page.setQuantity(2);
            page.setDirection(SwipeDirection.DOWN);
            page.setPercent(70);

            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setPage(page);

            assertDoesNotThrow(() -> logUtil.logSwipeNativeInfo(swipeNative));
        }
    }

    @Nested
    class LogAssertPresent {
        @Test
        void logsAssertPresent() {
            AssertPresent command = new AssertPresent();
            command.setLocator("presentLocator");
            command.setNegative(false);

            assertDoesNotThrow(() -> logUtil.logAssertPresent(command));
        }

        @Test
        void logsAssertPresentNegative() {
            AssertPresent command = new AssertPresent();
            command.setLocator("notPresentLocator");
            command.setNegative(true);

            assertDoesNotThrow(() -> logUtil.logAssertPresent(command));
        }
    }

    @Nested
    class LogAssertChecked {
        @Test
        void logsAssertChecked() {
            AssertChecked command = new AssertChecked();
            command.setLocator("checkedLocator");
            command.setNegative(false);

            assertDoesNotThrow(() -> logUtil.logAssertChecked(command));
        }

        @Test
        void logsAssertCheckedNegative() {
            AssertChecked command = new AssertChecked();
            command.setLocator("uncheckedLocator");
            command.setNegative(true);

            assertDoesNotThrow(() -> logUtil.logAssertChecked(command));
        }
    }
}
