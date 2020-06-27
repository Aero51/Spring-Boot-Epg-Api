//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.06.24 at 11:07:57 PM CEST 
//


package com.aero51.springbootepdapi.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}channel" maxOccurs="unbounded"/>
 *         &lt;element ref="{}programme" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="generator-info-name" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="generator-info-url" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "channel",
    "programme"
})
@XmlRootElement(name = "tv")
public class Tv {

    @XmlElement(required = true)
    protected List<Channel> channel;
    @XmlElement(required = true)
    protected List<Programme> programme;
    @XmlAttribute(name = "generator-info-name", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String generatorInfoName;
    @XmlAttribute(name = "generator-info-url", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String generatorInfoUrl;

    /**
     * Gets the value of the channel property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the channel property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChannel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Channel }
     * 
     * 
     */
    public List<Channel> getChannel() {
        if (channel == null) {
            channel = new ArrayList<Channel>();
        }
        return this.channel;
    }

    /**
     * Gets the value of the programme property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the programme property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProgramme().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Programme }
     * 
     * 
     */
    public List<Programme> getProgramme() {
        if (programme == null) {
            programme = new ArrayList<Programme>();
        }
        return this.programme;
    }

    /**
     * Gets the value of the generatorInfoName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneratorInfoName() {
        return generatorInfoName;
    }

    /**
     * Sets the value of the generatorInfoName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneratorInfoName(String value) {
        this.generatorInfoName = value;
    }

    /**
     * Gets the value of the generatorInfoUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneratorInfoUrl() {
        return generatorInfoUrl;
    }

    /**
     * Sets the value of the generatorInfoUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneratorInfoUrl(String value) {
        this.generatorInfoUrl = value;
    }

}
