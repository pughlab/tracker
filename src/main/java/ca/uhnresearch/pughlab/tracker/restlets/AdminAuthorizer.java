package ca.uhnresearch.pughlab.tracker.restlets;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

public class AdminAuthorizer extends Filter {
	
	/**
	 * Use Shiro to check whether we have an admin permission, and only allow the request to 
	 * continue if we do. Ideal for naive coarse-grained authorization.
	 */
	protected int beforeHandle(Request request, Response response) {
		
    	Subject currentUser = SecurityUtils.getSubject();
    	if (currentUser.isPermitted("admin")) {
    		return CONTINUE;
    	} else {
    		response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    		return STOP;
    	}
	}

}
