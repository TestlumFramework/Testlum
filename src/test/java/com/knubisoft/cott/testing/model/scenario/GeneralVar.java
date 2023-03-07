
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for generalVar complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="generalVar"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}var"&gt;
 *       &lt;group ref="{http://www.knubisoft.com/cott/testing/model/scenario}generalVarType"/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generalVar", propOrder = {
    "file",
    "path",
    "expression",
    "constant",
    "sql"
})
public class GeneralVar
    extends Var
{

    protected FromFile file;
    protected FromPath path;
    protected FromExpression expression;
    protected FromConstant constant;
    @XmlElement(name = "SQL")
    protected FromSQL sql;

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
     * Gets the value of the sql property.
     * 
     * @return
     *     possible object is
     *     {@link FromSQL }
     *     
     */
    public FromSQL getSQL() {
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
    public void setSQL(FromSQL value) {
        this.sql = value;
    }

}
