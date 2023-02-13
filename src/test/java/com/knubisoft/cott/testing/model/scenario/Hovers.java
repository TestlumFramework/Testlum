
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hovers complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hovers"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="hover" type="{http://www.knubisoft.com/cott/testing/model/scenario}hover" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="moveToEmptySpace" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hovers", propOrder = {
    "hover"
})
public class Hovers
    extends AbstractUiCommand
{

    protected List<Hover> hover;
    @XmlAttribute(name = "moveToEmptySpace")
    protected Boolean moveToEmptySpace;

    /**
     * Gets the value of the hover property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hover property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHover().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Hover }
     * 
     * 
     */
    public List<Hover> getHover() {
        if (hover == null) {
            hover = new ArrayList<Hover>();
        }
        return this.hover;
    }

    /**
     * Gets the value of the moveToEmptySpace property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMoveToEmptySpace() {
        if (moveToEmptySpace == null) {
            return false;
        } else {
            return moveToEmptySpace;
        }
    }

    /**
     * Sets the value of the moveToEmptySpace property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMoveToEmptySpace(Boolean value) {
        this.moveToEmptySpace = value;
    }

}
