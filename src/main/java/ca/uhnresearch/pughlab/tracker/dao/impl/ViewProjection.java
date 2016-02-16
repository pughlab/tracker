package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QView.views;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.domain.QView;
import ca.uhnresearch.pughlab.tracker.dto.View;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

public class ViewProjection extends MappingProjection<View> {
	
	private static final long serialVersionUID = -7719818879048907767L;
	
	private final Logger logger = LoggerFactory.getLogger(ViewProjection.class);

	private static ObjectMapper mapper = new ObjectMapper();

	public ViewProjection(QView views) {
        super(View.class, views.id, views.studyId, views.name, views.description, views.body, views.options);
    }

    @Override
    protected View map(Tuple tuple) {
    	View product = new View();

        product.setId(tuple.get(views.id));
        product.setStudyId(tuple.get(views.studyId));
        product.setName(tuple.get(views.name));
        product.setDescription(tuple.get(views.description));
        product.setBody(tuple.get(views.body));
        
        String options = tuple.get(views.options);
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
