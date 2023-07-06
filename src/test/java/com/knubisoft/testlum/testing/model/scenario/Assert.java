
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for assert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="assert"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="equal" type="{http://www.knubisoft.com/testlum/testing/model/scenario}equal"/&gt;
 *         &lt;element name="notEqual" type="{http://www.knubisoft.com/testlum/testing/model/scenario}notEqual"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assert", propOrder = {
    "equalOrNotEqual"
})
public class Assert
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "equal", type = Equal.class),
        @XmlElement(name = "notEqual", type = NotEqual.class)
    })
    protected List<Equality> equalOrNotEqual;

    /**
     * Gets the value of the equalOrNotEqual property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the equalOrNotEqual property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEqualOrNotEqual().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Equal }
     * {@link NotEqual }
     * 
     * 
     */
    public List<Equality> getEqualOrNotEqual() {
        if (equalOrNotEqual == null) {
            equalOrNotEqual = new ArrayList<Equality>();
        }
        return this.equalOrNotEqual;
    }

}
