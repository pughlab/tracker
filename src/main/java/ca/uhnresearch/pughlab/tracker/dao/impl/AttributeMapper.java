package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.util.HashMap;
import java.util.Map;

import ca.uhnresearch.pughlab.tracker.domain.QAttributes;
import ca.uhnresearch.pughlab.tracker.dto.Attributes;
import ca.uhnresearch.pughlab.tracker.validation.BooleanValueValidator;
import ca.uhnresearch.pughlab.tracker.validation.DateValueValidator;
import ca.uhnresearch.pughlab.tracker.validation.NumberValueValidator;
import ca.uhnresearch.pughlab.tracker.validation.OptionValueValidator;
import ca.uhnresearch.pughlab.tracker.validation.StringValueValidator;
import ca.uhnresearch.pughlab.tracker.validation.ValueValidator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.sql.RelationalPath;
import com.mysema.query.sql.dml.AbstractMapper;
import com.mysema.query.types.Path;

/**
 * Maps attributes into a form which can be written into the database.
 * 
 * @author stuartw
 */
public class AttributeMapper extends AbstractMapper<Attributes> {

	/**
	 * The list of available validators.
	 */
	private static final Map<String, ValueValidator> validators = 
					new HashMap<String, ValueValidator>();
	
	static {
		validators.put(Attributes.ATTRIBUTE_TYPE_STRING, new StringValueValidator());
		validators.put(Attributes.ATTRIBUTE_TYPE_BOOLEAN, new BooleanValueValidator());
		validators.put(Attributes.ATTRIBUTE_TYPE_OPTION, new OptionValueValidator());
		validators.put(Attributes.ATTRIBUTE_TYPE_DATE, new DateValueValidator());
		validators.put(Attributes.ATTRIBUTE_TYPE_NUMBER, new NumberValueValidator());
	}
	
	/**
	 * Retrieves the appropriate validator.
	 * @param type the type
	 * @return the validator
	 */
	public static ValueValidator getAttributeValidator(String type) {
		return validators.get(type);
	}

	/**
	 * Mapper to serialize Java values.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Main method to initialize a map for writing from the given bean.
	 * @param entity the entity
	 * @param bean the attribute object
	 * @return a map of paths to writable values
	 */
	@Override
    public Map<Path<?>, Object> createMap(RelationalPath<?> entity, Attributes bean) {
		final QAttributes attributes = QAttributes.attributes;
        final Map<Path<?>, Object> values = new HashMap<Path<?>, Object>();
        values.put(attributes.id, bean.getId());
        values.put(attributes.studyId, bean.getStudyId());
        values.put(attributes.name, bean.getName());
        values.put(attributes.label, bean.getLabel());
        values.put(attributes.description, bean.getDescription());
        values.put(attributes.rank, bean.getRank());
        values.put(attributes.type, bean.getType());
        
        try {
        	final JsonNode options = bean.getOptions();
        	if (options != null)
        		values.put(attributes.options, mapper.writeValueAsString(options));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        
        return values;
    }
}
