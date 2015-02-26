package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import org.codehaus.jackson.annotate.JsonProperty;

abstract class AbstractResponseDTO {
	
	@JsonProperty
	protected URL serviceUrl;

	public URL getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(URL serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	protected AbstractResponseDTO(URL serviceUrl) {
		super();
		this.serviceUrl = serviceUrl;
	}
}
