
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relationalDB.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="relationalDB"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="POSTGRES"/&gt;
 *     &lt;enumeration value="MYSQL"/&gt;
 *     &lt;enumeration value="ORACLE"/&gt;
 *     &lt;enumeration value="CLICKHOUSE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "relationalDB")
@XmlEnum
public enum RelationalDB {

    POSTGRES,
    MYSQL,
    ORACLE,
    CLICKHOUSE;

    public String value() {
        return name();
    }

    public static RelationalDB fromValue(String v) {
        return valueOf(v);
    }

}
