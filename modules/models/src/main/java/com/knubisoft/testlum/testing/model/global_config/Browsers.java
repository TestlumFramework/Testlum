
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for browsers complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="browsers"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="chrome" type="{http://www.knubisoft.com/testlum/testing/model/global-config}chrome"/&gt;
 *         &lt;element name="firefox" type="{http://www.knubisoft.com/testlum/testing/model/global-config}firefox"/&gt;
 *         &lt;element name="safari" type="{http://www.knubisoft.com/testlum/testing/model/global-config}safari"/&gt;
 *         &lt;element name="edge" type="{http://www.knubisoft.com/testlum/testing/model/global-config}edge"/&gt;
 *         &lt;element name="opera" type="{http://www.knubisoft.com/testlum/testing/model/global-config}opera"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "browsers", propOrder = {
    "chromeOrFirefoxOrSafari"
})
public class Browsers {

    @XmlElements({
        @XmlElement(name = "chrome", type = Chrome.class),
        @XmlElement(name = "firefox", type = Firefox.class),
        @XmlElement(name = "safari", type = Safari.class),
        @XmlElement(name = "edge", type = Edge.class),
        @XmlElement(name = "opera", type = Opera.class)
    })
    protected List<AbstractBrowser> chromeOrFirefoxOrSafari;

    /**
     * Gets the value of the chromeOrFirefoxOrSafari property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the chromeOrFirefoxOrSafari property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChromeOrFirefoxOrSafari().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Chrome }
     * {@link Firefox }
     * {@link Safari }
     * {@link Edge }
     * {@link Opera }
     * 
     * 
     */
    public List<AbstractBrowser> getChromeOrFirefoxOrSafari() {
        if (chromeOrFirefoxOrSafari == null) {
            chromeOrFirefoxOrSafari = new ArrayList<AbstractBrowser>();
        }
        return this.chromeOrFirefoxOrSafari;
    }

}
