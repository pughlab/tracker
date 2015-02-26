package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import org.codehaus.jackson.annotate.JsonProperty;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewResponseDTO extends AbstractResponseDTO {

	StudyDTO study;
	ViewDTO view;

	public ViewResponseDTO(URL url, Studies s, Views v) {
		super(url);
		this.study = new StudyDTO(s);
		this.view = new ViewDTO(v);
	}


	@JsonProperty
	public StudyDTO getStudy() {
		return study;
	}

	public void setStudy(StudyDTO study) {
		this.study = study;
	}

	@JsonProperty
	public ViewDTO getView() {
		return view;
	}

	public void setView(ViewDTO view) {
		this.view = view;
	}

}
