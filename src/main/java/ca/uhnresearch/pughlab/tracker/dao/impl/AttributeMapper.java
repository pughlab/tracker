package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.HashMap;
import java.util.Map;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.AbstractMapper;
import com.mysema.query.types.Path;

public class AttributeMapper extends AbstractMapper<Attributes> {

	private static ObjectMapper mapper = new ObjectMapper();

	@Override
    public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Attributes bean) {
        Map<Path<?>, Object> values = new HashMap<Path<?>, Object>();
        values.put(attributes.id, bean.getId());
        values.put(attributes.studyId, bean.getStudyId());
        values.put(attributes.name, bean.getName());
        values.put(attributes.label, bean.getLabel());
        values.put(attributes.description, bean.getDescription());
        values.put(attributes.rank, bean.getRank());
        values.put(attributes.type, bean.getType());
        
        try {
        	JsonNode options = bean.getOptions();
        	if (options != null)
        		values.put(attributes.options, mapper.writeValueAsString(options));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        
        return values;
    }
}
