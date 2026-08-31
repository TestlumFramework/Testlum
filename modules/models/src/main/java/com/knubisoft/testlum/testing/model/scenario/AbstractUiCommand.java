
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for abstractUiCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractUiCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractUiCommand")
@XmlSeeAlso({
    UiWait.class,
    WaitNative.class,
    WebVar.class,
    NativeVar.class,
    UiCondition.class,
    WebAssert.class,
    NativeAssert.class,
    AssertAttribute.class,
    AssertTitle.class,
    AssertAlert.class,
    UiAi.class,
    Javascript.class,
    Navigate.class,
    BrowserTab.class,
    Alert.class,
    HotKey.class,
    Copy.class,
    Cut.class,
    CommandWithOptionalLocator.class,
    SingleKeyCommand.class,
    Image.class,
    MobileImage.class,
    NativeImage.class,
    DragAndDrop.class,
    DragAndDropNative.class,
    Refresh.class,
    MobilebrowserRepeat.class,
    WebRepeat.class,
    WebView.class,
    NativeRepeat.class,
    NavigateNative.class,
    SwipeNative.class,
    CommandWithLocator.class,
    SwipePage.class
})
public abstract class AbstractUiCommand
    extends AbstractCommand
{


}
