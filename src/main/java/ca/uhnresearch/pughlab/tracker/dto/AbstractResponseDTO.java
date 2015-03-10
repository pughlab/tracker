package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

abstract class AbstractResponseDTO {
	
	private URL serviceUrl;
	private UserDTO user;
	
	@JsonProperty
	public URL getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(URL serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	@JsonProperty
	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

	protected AbstractResponseDTO(URL serviceUrl, UserDTO user) {
		super();
		this.serviceUrl = serviceUrl;
		this.user = user;
	}
}
