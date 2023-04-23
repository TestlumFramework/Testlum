
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for clear complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="clear"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;attribute name="highlight" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "clear")
public class Clear
    extends CommandWithLocator
{

    @XmlAttribute(name = "highlight", required = true)
    protected boolean highlight;

    /**
     * Gets the value of the highlight property.
     * 
     */
    public boolean isHighlight() {
        return highlight;
    }

    /**
     * Sets the value of the highlight property.
     * 
     */
    public void setHighlight(boolean value) {
        this.highlight = value;
    }

}
