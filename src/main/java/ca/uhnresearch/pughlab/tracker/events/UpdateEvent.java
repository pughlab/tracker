package ca.uhnresearch.pughlab.tracker.events;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class UpdateEvent {
	
	/**
	 * Event sent by the server to a client on initial connection
	 */
	public static final String EVENT_WELCOME = "welcome";
	
	/**
	 * Event sent by the client to the server notifying subscription to a scope
	 */
	public static final String EVENT_JOIN = "join";
	
	/**
	 * Event sent by the client to the server notifying a change to an entity field
	 */
	public static final String EVENT_SET_FIELD = "field";

	/**
	 * Event sent by the client to the server notifying a new record
	 */
	public static final String EVENT_NEW_RECORD = "record";

	/**
	 * Event sent by the client to the server notifying a new user has connected 
	 * to the same scope
	 */
	public static final String EVENT_USER_CONNECTED = "userconnect";

	/**
	 * Event sent by the client to the server notifying a user has disconnected from 
	 * the same scope
	 */
	public static final String EVENT_USER_DISCONNECTED = "userdisconnect";

	private String type;
	private EventData data = new EventData();
	
	/**
	 * Default constructor
	 */
	public UpdateEvent() { }
	
	/**
	 * Constructor that accepts an initial type
	 * @param type
	 */
	public UpdateEvent (String type) {
		this.type = type;
	}
	
	public class EventData {
		private String user;
		private String scope;
		private long time = (new Date()).getTime();
		private JsonNode parameters;

		@JsonProperty
		/**
		 * @return the user
		 */
		public String getUser() {
			return user;
		}

		/**
		 * @param parameters the user to set
		 */
		public void setUser(String user) {
			this.user = user;
		}

		@JsonProperty
		/**
		 * @return the time
		 */
		public long getTime() {
			return time;
		}

		/**
		 * @param parameters the time to set
		 */
		public void setTime(long time) {
			this.time = time;
		}

		@JsonProperty
		/**
		 * @return the scope
		 */
		public String getScope() {
			return scope;
		}

		/**
		 * @param parameters the scope to set
		 */
		public void setScope(String scope) {
			this.scope = scope;
		}

		/**
		 * @return the parameters
		 */
		@JsonProperty
		public JsonNode getParameters() {
			return parameters;
		}

		/**
		 * @param parameters the parameters to set
		 */
		public void setParameters(JsonNode parameters) {
			this.parameters = parameters;
		}
	}

	/**
	 * @return the type
	 */
	@JsonProperty
	public String getType() {
		return type;
	}

	/**
	 * @param parameters the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the data
	 */
	@JsonProperty
	public EventData getData() {
		return data;
	}

	/**
	 * @param parameters the data to set
	 */
	public void setData(EventData data) {
		this.data = data;
	}
}