
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


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
    Javascript.class,
    Navigate.class,
    BrowserTab.class,
    HotKey.class,
    Copy.class,
    Cut.class,
    SingleKeyCommand.class,
    CommandWithLocator.class,
    Image.class,
    DragAndDrop.class,
    DragAndDropNative.class,
    Refresh.class,
    MobilebrowserRepeat.class,
    WebRepeat.class,
    WebView.class,
    NativeRepeat.class,
    NavigateNative.class,
    CommandWithOptionalLocator.class
})
public abstract class AbstractUiCommand
    extends AbstractCommand
{


}
