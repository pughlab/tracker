package ca.uhnresearch.pughlab.tracker.sockets;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

public class UpdateEvent {
	
	public static final String WELCOME_EVENT = "welcome";

	private String sender;
	private String recipient;
	private String type;
	private JsonNode data;
	private long time;

	public UpdateEvent() {
		this("", "");
	}

	public UpdateEvent(String sender, String type) {
		this.sender = sender;
		this.type = type;
		this.time = new Date().getTime();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public JsonNode getData() {
		return data;
	}

	public void setData(JsonNode data) {
		this.data = data;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
}