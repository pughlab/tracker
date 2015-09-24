package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QStudy.studies;

import java.util.HashMap;
import java.util.Map;

import ca.uhnresearch.pughlab.tracker.dto.Study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.AbstractMapper;
import com.mysema.query.types.Path;

public class StudyMapper extends AbstractMapper<Study> {

	private static ObjectMapper mapper = new ObjectMapper();

	@Override
    public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Study bean) {
        Map<Path<?>, Object> values = new HashMap<Path<?>, Object>();
        values.put(studies.id, bean.getId());
        values.put(studies.name, bean.getName());
        values.put(studies.description, bean.getDescription());
        
        try {
        	JsonNode options = bean.getOptions();
        	if (options != null)
        		values.put(studies.options, mapper.writeValueAsString(options));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        
        return values;
    }

}
