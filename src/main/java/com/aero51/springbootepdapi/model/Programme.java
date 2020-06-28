//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.06.24 at 11:07:57 PM CEST 
//

package com.aero51.springbootepdapi.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}title"/>
 *         &lt;element ref="{}sub-title" minOccurs="0"/>
 *         &lt;element ref="{}desc" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}credits" minOccurs="0"/>
 *         &lt;element ref="{}date" minOccurs="0"/>
 *         &lt;element ref="{}category" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}icon" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="channel" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="stop" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "title", "subTitle", "desc", "credits", "date", "category", "icon" })
@XmlRootElement(name = "programme")
public class Programme {
	@XmlTransient
	private Integer db_id;
	@XmlElement(required = true)
	protected Title title;
	@XmlElement(name = "sub-title")
	protected SubTitle subTitle;
	protected List<Desc> desc;
	protected Credits credits;
	protected Integer date;
	protected List<Category> category;
	protected Icon icon;
	@XmlAttribute(name = "channel", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String channel;
	@XmlAttribute(name = "start", required = true)
	@XmlSchemaType(name = "anySimpleType")
	protected String start;
	@XmlAttribute(name = "stop", required = true)
	@XmlSchemaType(name = "anySimpleType")
	protected String stop;

	/**
	 * Gets the value of the title property.
	 * 
	 * @return possible object is {@link Title }
	 * 
	 */
	public Title getTitle() {
		return title;
	}

	/**
	 * Sets the value of the title property.
	 * 
	 * @param value allowed object is {@link Title }
	 * 
	 */
	public void setTitle(Title value) {
		this.title = value;
	}

	/**
	 * Gets the value of the subTitle property.
	 * 
	 * @return possible object is {@link SubTitle }
	 * 
	 */
	public SubTitle getSubTitle() {
		return subTitle;
	}

	/**
	 * Sets the value of the subTitle property.
	 * 
	 * @param value allowed object is {@link SubTitle }
	 * 
	 */
	public void setSubTitle(SubTitle value) {
		this.subTitle = value;
	}

	/**
	 * Gets the value of the desc property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot.
	 * Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
	 * for the desc property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDesc().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Desc }
	 * 
	 * 
	 */
	public List<Desc> getDesc() {
		if (desc == null) {
			desc = new ArrayList<Desc>();
		}
		return this.desc;
	}

	/**
	 * Gets the value of the credits property.
	 * 
	 * @return possible object is {@link Credits }
	 * 
	 */
	public Credits getCredits() {
		return credits;
	}

	/**
	 * Sets the value of the credits property.
	 * 
	 * @param value allowed object is {@link Credits }
	 * 
	 */
	public void setCredits(Credits value) {
		this.credits = value;
	}

	/**
	 * Gets the value of the date property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public Integer getDate() {
		return date;
	}

	/**
	 * Sets the value of the date property.
	 * 
	 * @param value allowed object is {@link BigInteger }
	 * 
	 */
	public void setDate(Integer value) {
		this.date = value;
	}

	/**
	 * Gets the value of the category property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot.
	 * Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
	 * for the category property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCategory().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Category }
	 * 
	 * 
	 */
	public List<Category> getCategory() {
		// if (category == null) {
		// category = new ArrayList<Category>();
		// }
		return category;
	}

	/**
	 * Gets the value of the icon property.
	 * 
	 * @return possible object is {@link Icon }
	 * 
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Sets the value of the icon property.
	 * 
	 * @param value allowed object is {@link Icon }
	 * 
	 */
	public void setIcon(Icon value) {
		this.icon = value;
	}

	/**
	 * Gets the value of the channel property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Sets the value of the channel property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setChannel(String value) {
		this.channel = value;
	}

	/**
	 * Gets the value of the start property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getStart() {
		return start;
	}

	/**
	 * Sets the value of the start property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setStart(String value) {
		this.start = value;
	}

	/**
	 * Gets the value of the stop property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getStop() {
		return stop;
	}

	/**
	 * Sets the value of the stop property.
	 * 
	 * @param value allowed object is {@link String }
	 * 
	 */
	public void setStop(String value) {
		this.stop = value;
	}

}
