package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractResponse {
	
	private URL serviceUrl;
	private User user;
	private Map<String, URL> actions = new HashMap<String, URL>();
		
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

	protected AbstractResponse() {}

	/**
	 * @return the actions
	 */
	@JsonProperty
	public Map<String, URL> getActions() {
		return actions;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(Map<String, URL> actions) {
		this.actions = actions;
	};
}
