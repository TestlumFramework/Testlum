package com.testlum.testing.model.global_config;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Java class for cryptographyIntegrations complex type.</p>
 *
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 *
 * <pre>{@code
 * <complexType name="cryptographyIntegrations">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="cryptography" type="{http://www.testlum.com/testing/model/global-config}cryptography" maxOccurs="unbounded"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cryptographyIntegrations", propOrder = {
        "cryptography"
})
public class CryptographyIntegrations {

    protected List<Cryptography> cryptography;

    /**
     * Gets the value of the cryptography property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cryptography property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCryptography().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Cryptography }
     *
     *
     */
    public List<Cryptography> getCryptography() {
        if (cryptography == null) {
            cryptography = new ArrayList<Cryptography>();
        }
        return this.cryptography;
    }

}