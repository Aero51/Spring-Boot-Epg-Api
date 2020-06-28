package com.aero51.springbootepdapi.model.output;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "onerowchannel")
public class OneRowChannel {

	@XmlTransient
	@Id
	// @GeneratedValue(strategy = GenerationType.AUTO)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer channel_db_id;

	@Column(length = 10000)
	private String id;

	@Column(length = 10000)
	private String display_name;

	@Column(length = 20000)
	private String combined;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getCombined() {
		return combined;
	}

	public void setCombined(String combined) {
		this.combined = combined;
	}

}
