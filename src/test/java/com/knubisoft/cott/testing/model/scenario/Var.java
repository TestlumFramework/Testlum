
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for var complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="var"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="relationalDbResult" type="{http://www.knubisoft.com/cott/testing/model/scenario}relationalDbResult"/&gt;
 *         &lt;element name="resultFrom" type="{http://www.knubisoft.com/cott/testing/model/scenario}resultFrom"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "var", propOrder = {
    "relationalDbResult",
    "resultFrom"
})
public class Var
    extends AbstractCommand
{

    protected RelationalDbResult relationalDbResult;
    protected ResultFrom resultFrom;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the relationalDbResult property.
     * 
     * @return
     *     possible object is
     *     {@link RelationalDbResult }
     *     
     */
    public RelationalDbResult getRelationalDbResult() {
        return relationalDbResult;
    }

    /**
     * Sets the value of the relationalDbResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationalDbResult }
     *     
     */
    public void setRelationalDbResult(RelationalDbResult value) {
        this.relationalDbResult = value;
    }

    /**
     * Gets the value of the resultFrom property.
     * 
     * @return
     *     possible object is
     *     {@link ResultFrom }
     *     
     */
    public ResultFrom getResultFrom() {
        return resultFrom;
    }

    /**
     * Sets the value of the resultFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResultFrom }
     *     
     */
    public void setResultFrom(ResultFrom value) {
        this.resultFrom = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
