
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for graphqlHttpMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="graphqlHttpMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="GET"/&gt;
 *     &lt;enumeration value="POST"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "graphqlHttpMethod")
@XmlEnum
public enum GraphqlHttpMethod {

    GET,
    POST;

    public String value() {
        return name();
    }

    public static GraphqlHttpMethod fromValue(String v) {
        return valueOf(v);
    }

}
