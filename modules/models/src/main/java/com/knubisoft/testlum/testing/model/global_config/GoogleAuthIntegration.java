
package com.knubisoft.testlum.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for googleAuthIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="googleAuthIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="googleAuth" type="{http://www.knubisoft.com/testlum/testing/model/global-config}googleAuth"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "googleAuthIntegration", propOrder = {
    "googleAuth"
})
public class GoogleAuthIntegration {

    protected List<GoogleAuth> googleAuth;

    /**
     * Gets the value of the googleAuth property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the googleAuth property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGoogleAuth().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GoogleAuth }
     * 
     * 
     */
    public List<GoogleAuth> getGoogleAuth() {
        if (googleAuth == null) {
            googleAuth = new ArrayList<GoogleAuth>();
        }
        return this.googleAuth;
    }

}
