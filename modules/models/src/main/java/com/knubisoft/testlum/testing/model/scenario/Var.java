
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for var complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="var"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;group ref="{http://www.knubisoft.com/testlum/testing/model/scenario}generalVarType"/&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "var", propOrder = {
    "file",
    "path",
    "constant",
    "expression",
    "sql",
    "generate"
})
public class Var
    extends AbstractCommand
{

    protected FromFile file;
    protected FromPath path;
    protected FromConstant constant;
    protected FromExpression expression;
    protected FromSQL sql;
    protected FromRandomGenerate generate;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link FromFile }
     *     
     */
    public FromFile getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromFile }
     *     
     */
    public void setFile(FromFile value) {
        this.file = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link FromPath }
     *     
     */
    public FromPath getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromPath }
     *     
     */
    public void setPath(FromPath value) {
        this.path = value;
    }

    /**
     * Gets the value of the constant property.
     * 
     * @return
     *     possible object is
     *     {@link FromConstant }
     *     
     */
    public FromConstant getConstant() {
        return constant;
    }

    /**
     * Sets the value of the constant property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromConstant }
     *     
     */
    public void setConstant(FromConstant value) {
        this.constant = value;
    }

    /**
     * Gets the value of the expression property.
     * 
     * @return
     *     possible object is
     *     {@link FromExpression }
     *     
     */
    public FromExpression getExpression() {
        return expression;
    }

    /**
     * Sets the value of the expression property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromExpression }
     *     
     */
    public void setExpression(FromExpression value) {
        this.expression = value;
    }

    /**
     * Gets the value of the sql property.
     * 
     * @return
     *     possible object is
     *     {@link FromSQL }
     *     
     */
    public FromSQL getSql() {
        return sql;
    }

    /**
     * Sets the value of the sql property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromSQL }
     *     
     */
    public void setSql(FromSQL value) {
        this.sql = value;
    }

    /**
     * Gets the value of the generate property.
     * 
     * @return
     *     possible object is
     *     {@link FromRandomGenerate }
     *     
     */
    public FromRandomGenerate getGenerate() {
        return generate;
    }

    /**
     * Sets the value of the generate property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromRandomGenerate }
     *     
     */
    public void setGenerate(FromRandomGenerate value) {
        this.generate = value;
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
