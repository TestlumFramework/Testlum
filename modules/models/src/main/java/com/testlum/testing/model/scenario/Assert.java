
package com.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for assert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="assert"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="equal" type="{http://www.testlum.com/testing/model/scenario}assertEqual"/&gt;
 *         &lt;element name="notEqual" type="{http://www.testlum.com/testing/model/scenario}assertNotEqual"/&gt;
 *         &lt;element name="jsonPath" type="{http://www.testlum.com/testing/model/scenario}assertJsonPath"/&gt;
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
    "equalOrNotEqualOrJsonPath"
})
public class Assert
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "equal", type = AssertEqual.class),
        @XmlElement(name = "notEqual", type = AssertNotEqual.class),
        @XmlElement(name = "jsonPath", type = AssertJsonPath.class)
    })
    protected List<AbstractCommand> equalOrNotEqualOrJsonPath;

    /**
     * Gets the value of the equalOrNotEqualOrJsonPath property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the equalOrNotEqualOrJsonPath property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEqualOrNotEqualOrJsonPath().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssertEqual }
     * {@link AssertNotEqual }
     * {@link AssertJsonPath }
     * 
     * 
     */
    public List<AbstractCommand> getEqualOrNotEqualOrJsonPath() {
        if (equalOrNotEqualOrJsonPath == null) {
            equalOrNotEqualOrJsonPath = new ArrayList<AbstractCommand>();
        }
        return this.equalOrNotEqualOrJsonPath;
    }

}
