
package com.knubisoft.cott.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nativeDevices complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nativeDevices"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="android" type="{http://www.knubisoft.com/cott/testing/model/global-config}androidDevice"/&gt;
 *         &lt;element name="ios" type="{http://www.knubisoft.com/cott/testing/model/global-config}iosDevice"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nativeDevices", propOrder = {
    "androidOrIos"
})
public class NativeDevices {

    @XmlElements({
        @XmlElement(name = "android", type = AndroidDevice.class),
        @XmlElement(name = "ios", type = IosDevice.class)
    })
    protected List<NativeDevice> androidOrIos;

    /**
     * Gets the value of the androidOrIos property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the androidOrIos property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAndroidOrIos().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AndroidDevice }
     * {@link IosDevice }
     * 
     * 
     */
    public List<NativeDevice> getAndroidOrIos() {
        if (androidOrIos == null) {
            androidOrIos = new ArrayList<NativeDevice>();
        }
        return this.androidOrIos;
    }

}
