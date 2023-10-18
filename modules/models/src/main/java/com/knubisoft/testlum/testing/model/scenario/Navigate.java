
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for navigate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="navigate"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="command" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}navigateCommand" /&gt;
 *       &lt;attribute name="path" type="{http://www.knubisoft.com/testlum/testing/model/scenario}slashStartedOrUrlString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "navigate")
public class Navigate
    extends AbstractUiCommand
{

    @XmlAttribute(name = "command", required = true)
    protected NavigateCommand command;
    @XmlAttribute(name = "path")
    protected String path;

    /**
     * Gets the value of the command property.
     * 
     * @return
     *     possible object is
     *     {@link NavigateCommand }
     *     
     */
    public NavigateCommand getCommand() {
        return command;
    }

    /**
     * Sets the value of the command property.
     * 
     * @param value
     *     allowed object is
     *     {@link NavigateCommand }
     *     
     */
    public void setCommand(NavigateCommand value) {
        this.command = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

}
