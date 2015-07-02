package ca.uhnresearch.pughlab.tracker.restlets;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.Authorizer;
import org.restlet.security.User;

public class SessionAuthorizer extends Authorizer {

	@Override
	protected boolean authorize(Request request, Response response) {
		Subject currentUser = SecurityUtils.getSubject();
		
		if (currentUser == null) {
			return false;
		} else {
			Object principal = currentUser.getPrincipal(); 
			if (principal != null) {
				User user = new User(principal.toString());
				request.getClientInfo().setUser(user);

				// It's not certain there is a challenge response
				if (request.getChallengeResponse() == null) {
					request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.CUSTOM));
					request.getChallengeResponse().setIdentifier(user.getIdentifier());
				}
				
			}
			return true;
		}
	}

}
