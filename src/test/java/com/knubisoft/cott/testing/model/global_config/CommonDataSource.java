
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for commonDataSource complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="commonDataSource"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}abstractDataSource"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="hikari" type="{http://www.knubisoft.com/cott/testing/model/global-config}hikari"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "commonDataSource", propOrder = {
    "hikari"
})
@XmlSeeAlso({
    Postgres.class,
    Mysql.class,
    Oracle.class
})
public abstract class CommonDataSource
    extends AbstractDataSource
{

    @XmlElement(required = true)
    protected Hikari hikari;

    /**
     * Gets the value of the hikari property.
     * 
     * @return
     *     possible object is
     *     {@link Hikari }
     *     
     */
    public Hikari getHikari() {
        return hikari;
    }

    /**
     * Sets the value of the hikari property.
     * 
     * @param value
     *     allowed object is
     *     {@link Hikari }
     *     
     */
    public void setHikari(Hikari value) {
        this.hikari = value;
    }

}
