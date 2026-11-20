package com.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for assertJsonPath complex type.
 *
 * <pre>
 * &lt;complexType name="assertJsonPath"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/scenario}abstractCommand"&gt;
 *       &lt;attribute name="path" use="required" type="{http://www.testlum.com/testing/model/scenario}jsonPathPattern" /&gt;
 *       &lt;attribute name="fromVar" type="{http://www.testlum.com/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="exists" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="equals" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="matches" type="{http://www.testlum.com/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "assertJsonPath")
public class AssertJsonPath
    extends AbstractCommand
{

    @XmlAttribute(name = "path", required = true)
    protected String path;
    @XmlAttribute(name = "fromVar")
    protected String fromVar;
    @XmlAttribute(name = "exists")
    protected Boolean exists;
    @XmlAttribute(name = "equals")
    protected String equals;
    @XmlAttribute(name = "matches")
    protected String matches;

    public String getPath() {
        return path;
    }

    public void setPath(String value) {
        this.path = value;
    }

    public String getFromVar() {
        return fromVar;
    }

    public void setFromVar(String value) {
        this.fromVar = value;
    }

    public Boolean isExists() {
        return exists;
    }

    public void setExists(Boolean value) {
        this.exists = value;
    }

    public String getEquals() {
        return equals;
    }

    public void setEquals(String value) {
        this.equals = value;
    }

    public String getMatches() {
        return matches;
    }

    public void setMatches(String value) {
        this.matches = value;
    }

}
