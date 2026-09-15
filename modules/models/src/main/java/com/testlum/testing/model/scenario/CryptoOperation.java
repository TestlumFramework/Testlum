package com.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cryptoOperation")
@XmlSeeAlso({Encrypt.class, Decrypt.class})
public abstract class CryptoOperation {

    @XmlAttribute(name = "value", required = true)
    protected String value;

    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) {
        this.alias = alias;
    }
    public abstract String getAction();
}
