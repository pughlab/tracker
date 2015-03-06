package ca.uhnresearch.pughlab.tracker.restlets;

import org.restlet.security.MapVerifier;

public class CustomMapVerifier extends MapVerifier {
	
	public CustomMapVerifier() {
		super();
		getLocalSecrets().put("user", "password".toCharArray());
	}
	
	public char[] getLocalSecret(String identifier) {
		char[] result = super.getLocalSecret(identifier);
		return result;
	}
}
