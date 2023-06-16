package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DRAG_AND_DROP_FILE_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.JavascriptConstant.QUERY_FOR_DRAG_AND_DROP;

@ExecutorForClass(DragAndDrop.class)
public class DragAndDropExecutor extends AbstractUiExecutor<DragAndDrop> {

    private final WebDriver driver;

    public DragAndDropExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        driver = dependencies.getDriver();
    }

    @Override
    public void execute(final DragAndDrop dragAndDrop, final CommandResult result) {
        LogUtil.logDragAndDropInfo(dragAndDrop);
        ResultUtil.addDragAndDropMetaDada(dragAndDrop, result);
        WebElement target = UiUtil.findWebElement(dependencies, dragAndDrop.getToLocatorId());
        if (StringUtils.isNotBlank(dragAndDrop.getFileName())) {
            File source = FileSearcher.searchFileFromDir(
                    dependencies.getFile().getParentFile(), dragAndDrop.getFileName());
            dropFile(target, source);
        } else {
            dropElement(target, UiUtil.findWebElement(dependencies, dragAndDrop.getFromLocatorId()));
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void dropElement(final WebElement target, final WebElement source) {
        Actions action = new Actions(driver);
        action.dragAndDrop(source, target)
                .build()
                .perform();
    }

    @SneakyThrows
    public void dropFile(final WebElement target, final File source) {
        if (!source.exists() || !source.isFile()) {
            throw new DefaultFrameworkException(DRAG_AND_DROP_FILE_NOT_FOUND, source.getName());
        }
        String JS_DROP_FILE = "for(var b=arguments[0],k=arguments[1],l=arguments[2],c=b.ownerDocument,m=0;;){var e=b.getBoundingClientRect(),g=e.left+(k||e.width/2),h=e.top+(l||e.height/2),f=c.elementFromPoint(g,h);if(f&&b.contains(f))break;if(1<++m)throw b=Error('Element not interractable'),b.code=15,b;b.scrollIntoView({behavior:'instant',block:'center',inline:'center'})}var a=c.createElement('INPUT');a.setAttribute('type','file');a.setAttribute('style','position:fixed;z-index:2147483647;left:0;top:0;');a.onchange=function(){var b={effectAllowed:'all',dropEffect:'none',types:['Files'],files:this.files,setData:function(){},getData:function(){},clearData:function(){},setDragImage:function(){}};window.DataTransferItemList&&(b.items=Object.setPrototypeOf([Object.setPrototypeOf({kind:'file',type:this.files[0].type,file:this.files[0],getAsFile:function(){return this.file},getAsString:function(b){var a=new FileReader;a.onload=function(a){b(a.target.result)};a.readAsText(this.file)}},DataTransferItem.prototype)],DataTransferItemList.prototype));Object.setPrototypeOf(b,DataTransfer.prototype);['dragenter','dragover','drop'].forEach(function(a){var d=c.createEvent('DragEvent');d.initMouseEvent(a,!0,!0,c.defaultView,0,0,0,g,h,!1,!1,!1,!1,0,null);Object.setPrototypeOf(d,null);d.dataTransfer=b;Object.setPrototypeOf(d,DragEvent.prototype);f.dispatchEvent(d)});a.parentElement.removeChild(a)};c.documentElement.appendChild(a);a.getBoundingClientRect();return a;";
        WebElement input = (WebElement) JavascriptUtil.executeJsScript(JS_DROP_FILE, driver, target, 0, 0);
        Path currentRelativePath = Paths.get(source.getAbsolutePath());
        String path = currentRelativePath.toAbsolutePath().toString();
        input.sendKeys(path);
    }
}
