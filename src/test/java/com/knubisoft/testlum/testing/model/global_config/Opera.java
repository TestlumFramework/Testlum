
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for opera complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="opera"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}abstractBrowser"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="operaOptionsArguments" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserOptionsArguments" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "opera", propOrder = {
    "operaOptionsArguments"
})
public class Opera
    extends AbstractBrowser
{

    protected BrowserOptionsArguments operaOptionsArguments;

    /**
     * Gets the value of the operaOptionsArguments property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserOptionsArguments }
     *     
     */
    public BrowserOptionsArguments getOperaOptionsArguments() {
        return operaOptionsArguments;
    }

    /**
     * Sets the value of the operaOptionsArguments property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserOptionsArguments }
     *     
     */
    public void setOperaOptionsArguments(BrowserOptionsArguments value) {
        this.operaOptionsArguments = value;
    }

}
