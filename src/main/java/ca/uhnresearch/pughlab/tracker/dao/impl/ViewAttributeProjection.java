package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QAttributes.attributes;
import static ca.uhnresearch.pughlab.tracker.domain.QViewAttributes.viewAttributes;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.domain.QAttributes;
import ca.uhnresearch.pughlab.tracker.domain.QViewAttributes;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

public class ViewAttributeProjection extends MappingProjection<ViewAttributes> {
	
	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static ObjectMapper mapper = new ObjectMapper();

	public ViewAttributeProjection(QAttributes attributes, QViewAttributes viewAttributes) {
        super(ViewAttributes.class, 
            attributes.id, attributes.studyId, attributes.name, attributes.description,
            attributes.label, viewAttributes.rank, attributes.type, attributes.options,
            viewAttributes.options);
    }

    @Override
    protected ViewAttributes map(Tuple tuple) {
    	final ViewAttributes product = new ViewAttributes();

        product.setId(tuple.get(attributes.id));
        product.setStudyId(tuple.get(attributes.studyId));
        product.setName(tuple.get(attributes.name));
        product.setDescription(tuple.get(attributes.description));
        product.setLabel(tuple.get(attributes.label));
        product.setRank(tuple.get(attributes.rank));
        product.setType(tuple.get(attributes.type));
        
        // Deserializing into a view attribute is more complicated, as basically
        // we need to deep merge the two sets of options.
        
        final String options = tuple.get(attributes.options);
		if (options != null) {
			try {
				product.setOptions(mapper.readValue(options, JsonNode.class));
			} catch (IOException e) {
				logger.error("Error in JSON attribute options", e.getMessage());
			}
		}

		final String viewOptions = tuple.get(viewAttributes.options);
		if (viewOptions != null) {
			try {
				product.setOptions(mapper.readValue(viewOptions, JsonNode.class));
			} catch (IOException e) {
				logger.error("Error in JSON attribute view options", e.getMessage());
			}
		}
		
        return product;
    }

}
