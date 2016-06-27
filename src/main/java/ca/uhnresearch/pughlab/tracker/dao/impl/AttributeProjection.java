package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.domain.QAttributes;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

final class AttributeProjection extends MappingProjection<Attributes> {
	
	private static final long serialVersionUID = -7719818879048907767L;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static ObjectMapper mapper = new ObjectMapper();

	public AttributeProjection(QAttributes attributes) {
        super(Attributes.class, 
        	attributes.id, attributes.studyId, attributes.name, attributes.description,
        	attributes.label, attributes.rank, attributes.type, attributes.options);
    }

    @Override
    protected Attributes map(Tuple tuple) {
    	final Attributes product = new Attributes();

        product.setId(tuple.get(attributes.id));
        product.setStudyId(tuple.get(attributes.studyId));
        product.setName(tuple.get(attributes.name));
        product.setDescription(tuple.get(attributes.description));
        product.setLabel(tuple.get(attributes.label));
        product.setRank(tuple.get(attributes.rank));
        product.setType(tuple.get(attributes.type));
        
        final String options = tuple.get(attributes.options);
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
