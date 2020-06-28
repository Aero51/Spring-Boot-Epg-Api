package com.aero51.springbootepdapi.model.pubproxy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pub_data")
public class Data {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer db_id;
	private String ip;
	private Integer port;
	private String country;
	private String last_checked;
	private String proxy_level;
	private String type;
	private Integer speed;
	// private List<Support> support

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLast_checked() {
		return last_checked;
	}

	public void setLast_checked(String last_checked) {
		this.last_checked = last_checked;
	}

	public String getProxy_level() {
		return proxy_level;
	}

	public void setProxy_level(String proxy_level) {
		this.proxy_level = proxy_level;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}
}
