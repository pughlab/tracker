package ca.uhnresearch.pughlab.tracker.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudyViewsResponse extends StudyResponse {
	
	List<View> views = new ArrayList<View>();

	public StudyViewsResponse() {
		super();
	}
	
	@JsonProperty
	public List<View> getViews() {
		return views;
	}

	public void setViews(List<View> views) {
		this.views = views;
	}
}
