
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for nativeAssert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nativeAssert"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="attribute" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertAttribute"/&gt;
 *         &lt;element name="equal" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertEqual"/&gt;
 *         &lt;element name="notEqual" type="{http://www.knubisoft.com/testlum/testing/model/scenario}assertNotEqual"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nativeAssert", propOrder = {
    "attributeOrEqualOrNotEqual"
})
public class NativeAssert
    extends AbstractUiCommand
{

    @XmlElements({
        @XmlElement(name = "attribute", type = AssertAttribute.class),
        @XmlElement(name = "equal", type = AssertEqual.class),
        @XmlElement(name = "notEqual", type = AssertNotEqual.class)
    })
    protected List<AbstractCommand> attributeOrEqualOrNotEqual;

    /**
     * Gets the value of the attributeOrEqualOrNotEqual property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeOrEqualOrNotEqual property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeOrEqualOrNotEqual().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssertAttribute }
     * {@link AssertEqual }
     * {@link AssertNotEqual }
     * 
     * 
     */
    public List<AbstractCommand> getAttributeOrEqualOrNotEqual() {
        if (attributeOrEqualOrNotEqual == null) {
            attributeOrEqualOrNotEqual = new ArrayList<AbstractCommand>();
        }
        return this.attributeOrEqualOrNotEqual;
    }

}
