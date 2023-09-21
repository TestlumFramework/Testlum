
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="file" type="{http://www.knubisoft.com/testlum/testing/model/scenario}s3File"/&gt;
 *         &lt;element name="bucket" type="{http://www.knubisoft.com/testlum/testing/model/scenario}s3Bucket"/&gt;
 *       &lt;/choice&gt;
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
    "fileOrBucket"
})
public class S3
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "file", type = S3File.class),
        @XmlElement(name = "bucket", type = S3Bucket.class)
    })
    protected List<AbstractCommand> fileOrBucket;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the fileOrBucket property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileOrBucket property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileOrBucket().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S3File }
     * {@link S3Bucket }
     * 
     * 
     */
    public List<AbstractCommand> getFileOrBucket() {
        if (fileOrBucket == null) {
            fileOrBucket = new ArrayList<AbstractCommand>();
        }
        return this.fileOrBucket;
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
