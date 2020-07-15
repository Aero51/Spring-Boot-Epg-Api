package com.aero51.springbootepdapi.model.gimmeproxy;

public class GimmeProxyResponseModel {

	private boolean supportsHttps;
	private String protocol;
	private String ip;
	private Integer port;
	private boolean get;
	private boolean post;
	private boolean cookies;
	private boolean referer;
	private Integer anonymityLevel;

	public boolean isSupportsHttps() {
		return supportsHttps;
	}

	public void setSupportsHttps(boolean supportsHttps) {
		this.supportsHttps = supportsHttps;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

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

	public boolean isGet() {
		return get;
	}

	public void setGet(boolean get) {
		this.get = get;
	}

	public boolean isPost() {
		return post;
	}

	public void setPost(boolean post) {
		this.post = post;
	}

	public boolean isCookies() {
		return cookies;
	}

	public void setCookies(boolean cookies) {
		this.cookies = cookies;
	}

	public boolean isReferer() {
		return referer;
	}

	public void setReferer(boolean referer) {
		this.referer = referer;
	}

	public Integer getAnonymityLevel() {
		return anonymityLevel;
	}

	public void setAnonymityLevel(Integer anonymityLevel) {
		this.anonymityLevel = anonymityLevel;
	}

}
