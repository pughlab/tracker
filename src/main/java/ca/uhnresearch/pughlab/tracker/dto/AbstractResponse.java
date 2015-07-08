package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractResponse {
	
	private URL serviceUrl;
	private User user;
		
	@JsonProperty
	public URL getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(URL serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	@JsonProperty
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	protected AbstractResponse() {};
}
