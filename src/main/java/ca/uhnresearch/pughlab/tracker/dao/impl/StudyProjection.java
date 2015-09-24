package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ca.uhnresearch.pughlab.tracker.domain.QStudy.studies;
import ca.uhnresearch.pughlab.tracker.domain.QStudy;
import ca.uhnresearch.pughlab.tracker.dto.Study;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

public class StudyProjection extends MappingProjection<Study> {
	
	private final Logger logger = LoggerFactory.getLogger(StudyProjection.class);

	private static ObjectMapper mapper = new ObjectMapper();

	public StudyProjection(QStudy study) {
		super(Study.class, study.id, study.name, study.description, study.options);
	}

	/**
	 * Generated ID
	 */
	private static final long serialVersionUID = -1039933824645084039L;

	@Override
	protected Study map(Tuple tuple) {
		Study product = new Study();

        product.setId(tuple.get(studies.id));
        product.setName(tuple.get(studies.name));
        product.setDescription(tuple.get(studies.description));
        
        String options = tuple.get(studies.options);
		if (options != null) {
			try {
				product.setOptions(mapper.readValue(options, JsonNode.class));
			} catch (IOException e) {
				logger.error("Error in JSON attribute options", e.getMessage());
			}
		}

        return product;
	}

}
