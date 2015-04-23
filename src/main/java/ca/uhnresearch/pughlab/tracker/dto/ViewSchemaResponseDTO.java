package ca.uhnresearch.pughlab.tracker.dto;

import java.net.URL;

import ca.uhnresearch.pughlab.tracker.domain.Studies;
import ca.uhnresearch.pughlab.tracker.domain.Views;

public class ViewSchemaResponseDTO extends ViewResponseDTO {

	public ViewSchemaResponseDTO() {
		super();
	}

	public ViewSchemaResponseDTO(URL url, UserDTO user, Studies s, Views v) {
		super(url, user, s, v);
	}

}
