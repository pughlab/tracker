package ca.uhnresearch.pughlab.tracker.dao.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhnresearch.pughlab.tracker.domain.QView;
import ca.uhnresearch.pughlab.tracker.dto.View;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.query.Tuple;
import com.mysema.query.types.MappingProjection;

/**
 * A projection to map views from the database into a {@link View}.
 * 
 * @author stuartw
 */
public class ViewProjection extends MappingProjection<View> {
	
	/**
	 * A serial identifier for serialization.
	 */
	private static final long serialVersionUID = -7719818879048907767L;
	
	/**
	 * A logger.
	 */
	private final Logger logger = LoggerFactory.getLogger(ViewProjection.class);

	/**
	 * A JSON object mapper.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Constructs a new projection instance from a {@link QView}.
	 * @param views the incoming view
	 */
	public ViewProjection(QView views) {
        super(View.class, views.id, views.studyId, views.name, views.description, views.body, views.options);
    }

	/**
	 * Maps a tuple into a view. Note that the options is read from JSON and transformed 
	 * into a JSON node.
	 * 
	 * @param tuple the incoming tuple
	 * @return a {@link View}
	 */
    @Override
    protected View map(Tuple tuple) {
    	final View product = new View();
    	final QView views = QView.views;

        product.setId(tuple.get(views.id));
        product.setStudyId(tuple.get(views.studyId));
        product.setName(tuple.get(views.name));
        product.setDescription(tuple.get(views.description));
        product.setBody(tuple.get(views.body));
        
        final String options = tuple.get(views.options);
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
