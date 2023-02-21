
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
 *         &lt;element name="fromFile" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromFile"/&gt;
 *         &lt;element name="fromPath" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromPath"/&gt;
 *         &lt;element name="fromExpression" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromExpression"/&gt;
 *         &lt;element name="fromConstant" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromConstant"/&gt;
 *         &lt;element name="fromCookie" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromCookie"/&gt;
 *         &lt;element name="fromDom" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromDom"/&gt;
 *         &lt;element name="fromSQL" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromSQL"/&gt;
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
    "fromFile",
    "fromPath",
    "fromExpression",
    "fromConstant",
    "fromCookie",
    "fromDom",
    "fromSQL"
})
public class Var
    extends AbstractCommand
{

    protected FromFile fromFile;
    protected FromPath fromPath;
    protected FromExpression fromExpression;
    protected FromConstant fromConstant;
    protected FromCookie fromCookie;
    protected FromDom fromDom;
    protected FromSQL fromSQL;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the fromFile property.
     * 
     * @return
     *     possible object is
     *     {@link FromFile }
     *     
     */
    public FromFile getFromFile() {
        return fromFile;
    }

    /**
     * Sets the value of the fromFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromFile }
     *     
     */
    public void setFromFile(FromFile value) {
        this.fromFile = value;
    }

    /**
     * Gets the value of the fromPath property.
     * 
     * @return
     *     possible object is
     *     {@link FromPath }
     *     
     */
    public FromPath getFromPath() {
        return fromPath;
    }

    /**
     * Sets the value of the fromPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromPath }
     *     
     */
    public void setFromPath(FromPath value) {
        this.fromPath = value;
    }

    /**
     * Gets the value of the fromExpression property.
     * 
     * @return
     *     possible object is
     *     {@link FromExpression }
     *     
     */
    public FromExpression getFromExpression() {
        return fromExpression;
    }

    /**
     * Sets the value of the fromExpression property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromExpression }
     *     
     */
    public void setFromExpression(FromExpression value) {
        this.fromExpression = value;
    }

    /**
     * Gets the value of the fromConstant property.
     * 
     * @return
     *     possible object is
     *     {@link FromConstant }
     *     
     */
    public FromConstant getFromConstant() {
        return fromConstant;
    }

    /**
     * Sets the value of the fromConstant property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromConstant }
     *     
     */
    public void setFromConstant(FromConstant value) {
        this.fromConstant = value;
    }

    /**
     * Gets the value of the fromCookie property.
     * 
     * @return
     *     possible object is
     *     {@link FromCookie }
     *     
     */
    public FromCookie getFromCookie() {
        return fromCookie;
    }

    /**
     * Sets the value of the fromCookie property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromCookie }
     *     
     */
    public void setFromCookie(FromCookie value) {
        this.fromCookie = value;
    }

    /**
     * Gets the value of the fromDom property.
     * 
     * @return
     *     possible object is
     *     {@link FromDom }
     *     
     */
    public FromDom getFromDom() {
        return fromDom;
    }

    /**
     * Sets the value of the fromDom property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromDom }
     *     
     */
    public void setFromDom(FromDom value) {
        this.fromDom = value;
    }

    /**
     * Gets the value of the fromSQL property.
     * 
     * @return
     *     possible object is
     *     {@link FromSQL }
     *     
     */
    public FromSQL getFromSQL() {
        return fromSQL;
    }

    /**
     * Sets the value of the fromSQL property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromSQL }
     *     
     */
    public void setFromSQL(FromSQL value) {
        this.fromSQL = value;
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
