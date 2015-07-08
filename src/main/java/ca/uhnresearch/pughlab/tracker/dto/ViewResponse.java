package ca.uhnresearch.pughlab.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewResponse extends StudyResponse {

	View view;
	
	public ViewResponse() {
		super();
	}
	
	@JsonProperty
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}
}
