
package com.knubisoft.testlum.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mysqlIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mysqlIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="mysql" type="{http://www.knubisoft.com/testlum/testing/model/global-config}mysql"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mysqlIntegration", propOrder = {
    "mysql"
})
public class MysqlIntegration {

    protected List<Mysql> mysql;

    /**
     * Gets the value of the mysql property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mysql property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMysql().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Mysql }
     * 
     * 
     */
    public List<Mysql> getMysql() {
        if (mysql == null) {
            mysql = new ArrayList<Mysql>();
        }
        return this.mysql;
    }

}
