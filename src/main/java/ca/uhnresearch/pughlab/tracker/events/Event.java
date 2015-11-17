package ca.uhnresearch.pughlab.tracker.events;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class defines the shape of an event within the tracker. The data field
 * is serialized and sent through websocket connections. The properties field is
 * not, and can be used for internal server-side data. For example, this might 
 * contain an actual value which we don't send because it might be confidential. 
 * 
 * @author Stuart Watt
 */
public class Event {
	
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
	 * Event sent by the client to the server notifying a change to an entity state
	 */
	public static final String EVENT_STATE = "state";

	/**
	 * Event sent by the client to the server notifying a new record
	 */
	public static final String EVENT_NEW_RECORD = "record";

	/**
	 * Event sent by the client to the server notifying a record is being deleted
	 */
	public static final String EVENT_DELETE_RECORD = "delete";

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
	private Map<String, Object> properties = new HashMap<String, Object>();
	
	/**
	 * Default constructor
	 */
	public Event() { }
	
	/**
	 * Constructor that accepts an initial type
	 * @param type
	 */
	public Event (String type) {
		this.type = type;
	}
	
	public class EventData {
		private String user;
		private String scope;
		private long time = (new Date()).getTime();
		private ObjectNode parameters;

		@JsonProperty
		/**
		 * @return the user
		 */
		public String getUser() {
			return user;
		}

		/**
		 * @param user the user to set
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
		 * @param time the time to set
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
		 * @param scope the scope to set
		 */
		public void setScope(String scope) {
			this.scope = scope;
		}

		/**
		 * @return the parameters
		 */
		@JsonProperty
		public ObjectNode getParameters() {
			return parameters;
		}

		/**
		 * @param parameters the parameters to set
		 */
		public void setParameters(ObjectNode parameters) {
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
	 * @param type the type to set
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
	 * @param data the data to set
	 */
	public void setData(EventData data) {
		this.data = data;
	}

	/**
	 * @return the properties
	 */
	@JsonIgnore
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}