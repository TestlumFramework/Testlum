
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for clickhouseIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="clickhouseIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="clickhouse" type="{http://www.knubisoft.com/testlum/testing/model/global-config}clickhouse"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "clickhouseIntegration", propOrder = {
    "clickhouse"
})
public class ClickhouseIntegration {

    protected List<Clickhouse> clickhouse;

    /**
     * Gets the value of the clickhouse property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clickhouse property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClickhouse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Clickhouse }
     * 
     * 
     */
    public List<Clickhouse> getClickhouse() {
        if (clickhouse == null) {
            clickhouse = new ArrayList<Clickhouse>();
        }
        return this.clickhouse;
    }

}
