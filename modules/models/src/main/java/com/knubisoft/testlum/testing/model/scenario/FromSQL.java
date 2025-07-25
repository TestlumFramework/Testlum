
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for fromSQL complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fromSQL"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="query" type="{http://www.knubisoft.com/testlum/testing/model/scenario}variableQuery"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="dbType" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}relationalDB" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fromSQL", propOrder = {
    "query"
})
public class FromSQL {

    @XmlElement(required = true)
    protected String query;
    @XmlAttribute(name = "dbType", required = true)
    protected RelationalDB dbType;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the query property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuery(String value) {
        this.query = value;
    }

    /**
     * Gets the value of the dbType property.
     * 
     * @return
     *     possible object is
     *     {@link RelationalDB }
     *     
     */
    public RelationalDB getDbType() {
        return dbType;
    }

    /**
     * Sets the value of the dbType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelationalDB }
     *     
     */
    public void setDbType(RelationalDB value) {
        this.dbType = value;
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

}
