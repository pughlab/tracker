package ca.uhnresearch.pughlab.tracker.resource;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.resource.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Spring-friendly directory, which makes getting the context easy, and therefore allows it
 * to be used when handling data that we find. 
 */
public class SpringDirectory extends Directory {
	
	/**
	 * A logger
	 */
	private final Logger logger = LoggerFactory.getLogger(SpringDirectory.class);

	/**
     * Constructor with a parent context.
     * 
     * @param context
     *            The parent context.
     */
    public SpringDirectory(Context context, Reference rootLocalReference) {
        super(context, rootLocalReference);
    	logger.debug("Constructor called with context: {}", context.toString());
    }
 
    /**
     * Constructor with a parent Restlet.
     * 
     * @param parent
     *            The parent Restlet.
     */
    public SpringDirectory(Restlet parent, Reference rootLocalReference) {
    	super(parent.getContext(), rootLocalReference);
    	logger.debug("Constructor called with context: {}", parent.getContext().toString());
    }

    public Reference getRootRef() {
    	Reference called = super.getRootRef();
    	logger.debug("Someone asked for this: {}", called.toString());
    	return called;
    }
}
