package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewResponseDTO extends AbstractResponseDTO {

	StudyDTO study;
	ViewDTO view;
	CountsDTO counts = new CountsDTO();
	List<AttributeDTO> attributes = new ArrayList<AttributeDTO>();
	List<JsonNode> records = new ArrayList<JsonNode>();

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

	@JsonProperty
	public List<AttributeDTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDTO> attributes) {
		this.attributes = attributes;
	}

	@JsonProperty
	public List<JsonNode> getRecords() {
		return records;
	}

	public void setRecords(List<JsonNode> records) {
		this.records = records;
	}

	@JsonProperty
	public CountsDTO getCounts() {
		return counts;
	}

	public void setCounts(CountsDTO counts) {
		this.counts = counts;
	}
}
