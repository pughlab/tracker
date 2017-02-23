package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QView.views;

import java.util.HashMap;
import java.util.Map;

import ca.uhnresearch.pughlab.tracker.dto.View;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.AbstractMapper;
import com.mysema.query.types.Path;

public class ViewMapper extends AbstractMapper<View> {

	private static ObjectMapper mapper = new ObjectMapper();

	@Override
    public Map<Path<?>, Object> createMap(RelationalPath<?> entity, View bean) {
		final Map<Path<?>, Object> values = new HashMap<Path<?>, Object>();
        values.put(views.id, bean.getId());
        values.put(views.studyId, bean.getStudyId());
        values.put(views.name, bean.getName());
        values.put(views.description, bean.getDescription());
        values.put(views.body, bean.getBody());
        
        try {
        	final JsonNode options = bean.getOptions();
        	if (options != null)
        		values.put(views.options, mapper.writeValueAsString(options));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        
        return values;
    }
}
