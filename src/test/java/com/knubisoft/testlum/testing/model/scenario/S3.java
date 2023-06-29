
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s3 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="s3"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/testlum/testing/model/scenario}s3File"/&gt;
 *         &lt;element name="bucket" type="{http://www.knubisoft.com/testlum/testing/model/scenario}s3Bucket"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s3", propOrder = {
    "file",
    "bucket"
})
public class S3
    extends AbstractCommand
{

    @XmlElement(required = true)
    protected S3File file;
    @XmlElement(required = true)
    protected S3Bucket bucket;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link S3File }
     *     
     */
    public S3File getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link S3File }
     *     
     */
    public void setFile(S3File value) {
        this.file = value;
    }

    /**
     * Gets the value of the bucket property.
     * 
     * @return
     *     possible object is
     *     {@link S3Bucket }
     *     
     */
    public S3Bucket getBucket() {
        return bucket;
    }

    /**
     * Sets the value of the bucket property.
     * 
     * @param value
     *     allowed object is
     *     {@link S3Bucket }
     *     
     */
    public void setBucket(S3Bucket value) {
        this.bucket = value;
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

}
