package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewResponse extends StudyResponse {

	View view;
	
	public ViewResponse() {
		super();
	}
	
	public ViewResponse(URL url, User user, Study s, View v) {
		super(url, user, s);
		this.view = v;
	}

	@JsonProperty
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}
}
