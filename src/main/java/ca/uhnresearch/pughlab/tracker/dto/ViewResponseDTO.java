package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewResponseDTO extends StudyResponseDTO {

	ViewDTO view;

	public ViewResponseDTO(URL url, UserDTO user, Studies s, Views v) {
		super(url, user, s);
		this.view = new ViewDTO(v);
	}

	@JsonProperty
	public ViewDTO getView() {
		return view;
	}

	public void setView(ViewDTO view) {
		this.view = view;
	}
}
