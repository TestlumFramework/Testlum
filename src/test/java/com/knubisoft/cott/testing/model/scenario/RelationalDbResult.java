
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relationalDbResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relationalDbResult"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="query" type="{http://www.knubisoft.com/cott/testing/model/scenario}variableQuery"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="file" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="dbType" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}relationalDB" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relationalDbResult", propOrder = {
    "query"
})
public class RelationalDbResult
    extends AbstractCommand
{

    @XmlElement(required = true)
    protected String query;
    @XmlAttribute(name = "file")
    protected String file;
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
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFile(String value) {
        this.file = value;
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
