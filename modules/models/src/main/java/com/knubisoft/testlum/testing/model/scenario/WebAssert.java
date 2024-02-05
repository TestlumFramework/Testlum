
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webAssert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webAssert"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="attribute" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertAttribute"/&gt;
 *         &lt;element name="title" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertTitle"/&gt;
 *         &lt;element name="equal" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertEqual"/&gt;
 *         &lt;element name="notEqual" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertNotEqual"/&gt;
 *         &lt;element name="alert" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertAlert"/&gt;
 *         &lt;element name="checked" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertChecked"/&gt;
 *         &lt;element name="present" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertPresent"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webAssert", propOrder = {
    "attributeOrTitleOrEqual"
})
public class WebAssert
    extends AbstractUiCommand
{

    @XmlElements({
        @XmlElement(name = "attribute", type = AssertAttribute.class),
        @XmlElement(name = "title", type = AssertTitle.class),
        @XmlElement(name = "equal", type = AssertEqual.class),
        @XmlElement(name = "notEqual", type = AssertNotEqual.class),
        @XmlElement(name = "alert", type = AssertAlert.class),
        @XmlElement(name = "checked", type = AssertChecked.class),
        @XmlElement(name = "present", type = AssertPresent.class)
    })
    protected List<AbstractCommand> attributeOrTitleOrEqual;

    /**
     * Gets the value of the attributeOrTitleOrEqual property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeOrTitleOrEqual property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeOrTitleOrEqual().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssertAttribute }
     * {@link AssertTitle }
     * {@link AssertEqual }
     * {@link AssertNotEqual }
     * {@link AssertAlert }
     * {@link AssertChecked }
     * {@link AssertPresent }
     * 
     * 
     */
    public List<AbstractCommand> getAttributeOrTitleOrEqual() {
        if (attributeOrTitleOrEqual == null) {
            attributeOrTitleOrEqual = new ArrayList<AbstractCommand>();
        }
        return this.attributeOrTitleOrEqual;
    }

}
