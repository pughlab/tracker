package ca.uhnresearch.pughlab.tracker.restlets;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultingFilter extends Filter {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private String defaultPath = "/index.html";
	
	public DefaultingFilter(Context context, Restlet next) {
		super(context, next);
	}
	
	@Override
	protected int doHandle (Request request, Response response) {
		logger.debug("doHandle FILTER: " + request.getResourceRef().getPath());
		int result = super.doHandle(request, response);
		if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
			
			final String path = request.getResourceRef().getPath();

			if (! getDefaultPath().equals(path)) {
				response.setStatus(Status.SUCCESS_OK);
				request.getResourceRef().setPath(getDefaultPath());
				result = super.doHandle(request, response);
			}
		}
		return result;
	}
	
	/**
	 * @return the defaultPath
	 */
	public String getDefaultPath() {
		return defaultPath;
	}

	/**
	 * @param defaultPath the defaultPath to set
	 */
	public void setDefaultPath(String defaultPath) {
		this.defaultPath = defaultPath;
	}
}
