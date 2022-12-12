
package com.knubisoft.cott.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for websockets complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="websockets"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="api" type="{http://www.knubisoft.com/cott/testing/model/global-config}websocketApi" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "websockets", propOrder = {
    "api"
})
public class Websockets {

    protected List<WebsocketApi> api;

    /**
     * Gets the value of the api property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the api property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApi().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebsocketApi }
     * 
     * 
     */
    public List<WebsocketApi> getApi() {
        if (api == null) {
            api = new ArrayList<WebsocketApi>();
        }
        return this.api;
    }

}
