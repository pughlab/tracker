package ca.uhnresearch.pughlab.tracker.events;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateEvent {
	
	public static final String EVENT_WELCOME = "welcome";
	
	public static final String EVENT_JOIN = "join";
	
	public static final String EVENT_SET_FIELD = "field";

	public static final String EVENT_NEW_RECORD = "record";

	public static final String EVENT_USER_CONNECTED = "connect";

	public static final String EVENT_USER_DISCONNECTED = "disconnect";

	private String type;
	private EventData data = new EventData();
	
	public UpdateEvent() { }
	
	public UpdateEvent (String type) {
		this.type = type;
	}
	
	public class EventData {
		private String user;
		private String scope;
		private long time = (new Date()).getTime();

		@JsonProperty
		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		@JsonProperty
		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		@JsonProperty
		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

	}

	@JsonProperty
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty
	public EventData getData() {
		return data;
	}

	public void setData(EventData data) {
		this.data = data;
	}
}