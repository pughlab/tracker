package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QViewAttributes.viewAttributes;

import java.util.HashMap;
import java.util.Map;

import ca.uhnresearch.pughlab.tracker.dto.View;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.AbstractMapper;
import com.mysema.query.types.Path;

public class ViewAttributeMapper extends AbstractMapper<ViewAttributes> {
	
	private View view;
	
	public ViewAttributeMapper(View view) {
		this.view = view;
	}

	private static ObjectMapper mapper = new ObjectMapper();

	@Override
	public Map<Path<?>, Object> createMap(RelationalPath<?> path, ViewAttributes bean) {
        
		final Map<Path<?>, Object> values = new HashMap<Path<?>, Object>();
        values.put(viewAttributes.attributeId, bean.getId());
        values.put(viewAttributes.viewId, view.getId());
        values.put(viewAttributes.rank, bean.getRank());
        
        try {
        	final JsonNode options = bean.getViewOptions();
        	if (options != null)
        		values.put(viewAttributes.options, mapper.writeValueAsString(options));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        
        return values;
	}

}
