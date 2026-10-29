
package com.testlum.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for authStrategies complex type&lt;/p&gt;.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 *
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="authStrategies"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="jwt" type="{http://www.testlum.com/testing/model/global-config}jwtAuthStrategy"/&gt;
 *         &lt;element name="basic" type="{http://www.testlum.com/testing/model/global-config}basicAuthStrategy"/&gt;
 *         &lt;element name="custom" type="{http://www.testlum.com/testing/model/global-config}customAuthStrategy"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "authStrategies", propOrder = {
    "jwtOrBasicOrCustom"
})
public class AuthStrategies {

    @XmlElements({
        @XmlElement(name = "jwt", type = JwtAuthStrategy.class),
        @XmlElement(name = "basic", type = BasicAuthStrategy.class),
        @XmlElement(name = "custom", type = CustomAuthStrategy.class)
    })
    protected List<AuthStrategy> jwtOrBasicOrCustom;

    /**
     * Gets the value of the jwtOrBasicOrCustom property.
     *
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the jwtOrBasicOrCustom property.</p>
     *
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getJwtOrBasicOrCustom().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BasicAuthStrategy }
     * {@link CustomAuthStrategy }
     * {@link JwtAuthStrategy }
     * </p>
     *
     *
     * @return
     *     The value of the jwtOrBasicOrCustom property.
     */
    public List<AuthStrategy> getJwtOrBasicOrCustom() {
        if (jwtOrBasicOrCustom == null) {
            jwtOrBasicOrCustom = new ArrayList<>();
        }
        return this.jwtOrBasicOrCustom;
    }

}
