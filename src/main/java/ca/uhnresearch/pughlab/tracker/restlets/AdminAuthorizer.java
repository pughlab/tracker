package ca.uhnresearch.pughlab.tracker.restlets;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authorizer;

public class AdminAuthorizer extends Authorizer {
	
	AdminAuthorizer() {
		setIdentifier("admin");
	}
	
	/**
	 * Use Shiro to check whether we have an admin permission, and only allow the request to 
	 * continue if we do. Ideal for naive coarse-grained authorization.
	 */
	@Override
	protected boolean authorize(Request request, Response response) {
		Subject currentUser = SecurityUtils.getSubject();
		return currentUser.isPermitted("admin");
	}

}
