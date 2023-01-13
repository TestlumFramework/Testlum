
package com.knubisoft.cott.testing.model.scenario;

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
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
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
    Wait.class,
    Javascript.class,
    Hovers.class,
    Navigate.class,
    CloseTab.class,
    Scroll.class,
    Image.class,
    DragAndDrop.class,
    DragAndDropNative.class,
    ScrollNative.class,
    SwipeNative.class,
    Refresh.class,
    HotKey.class,
    Copy.class,
    Paste.class,
    Cut.class,
    Highlight.class,
    Tab.class,
    Enter.class,
    BackSpace.class,
    Escape.class,
    Space.class,
    WebView.class,
    NavigateNative.class,
    CommandWithLocator.class
})
public abstract class AbstractUiCommand
    extends AbstractCommand
{


}
