
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for storageName.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="storageName"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="POSTGRES"/&gt;
 *     &lt;enumeration value="MYSQL"/&gt;
 *     &lt;enumeration value="ORACLE"/&gt;
 *     &lt;enumeration value="REDIS"/&gt;
 *     &lt;enumeration value="MONGODB"/&gt;
 *     &lt;enumeration value="DYNAMO"/&gt;
 *     &lt;enumeration value="CLICKHOUSE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "storageName")
@XmlEnum
public enum StorageName {

    POSTGRES,
    MYSQL,
    ORACLE,
    REDIS,
    MONGODB,
    DYNAMO,
    CLICKHOUSE;

    public String value() {
        return name();
    }

    public static StorageName fromValue(String v) {
        return valueOf(v);
    }

}
