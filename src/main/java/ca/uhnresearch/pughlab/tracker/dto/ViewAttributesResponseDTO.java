package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewAttributesResponseDTO extends ViewResponseDTO {
	
	public ViewAttributesResponseDTO() {
		super();
	}
	
	public ViewAttributesResponseDTO(URL url, UserDTO user, Studies s, Views v) {
		super(url, user, s, v);
	}

	List<AttributeDTO> attributes = new ArrayList<AttributeDTO>();

	@JsonProperty
	public List<AttributeDTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDTO> attributes) {
		this.attributes = attributes;
	}

	ViewPermissionsDTO permissions = new ViewPermissionsDTO();

	@JsonProperty
	public ViewPermissionsDTO getPermissions() {
		return permissions;
	}

	public void setPermissions(ViewPermissionsDTO permissions) {
		this.permissions = permissions;
	}
}
