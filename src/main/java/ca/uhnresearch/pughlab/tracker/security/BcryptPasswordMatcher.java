package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.mindrot.jbcrypt.BCrypt;

public class BcryptPasswordMatcher implements CredentialsMatcher {

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		
		UsernamePasswordToken userToken = (UsernamePasswordToken) token;
		String password = new String(userToken.getPassword());
		char[] credentials = (char[]) info.getCredentials();
		String hashed = new String(credentials);
		
		// We might get hashes from either the fixed or non-fixed blowfish.
		// This (nasty) hack ensures we still get a match.
		if (hashed.startsWith("$2y$")) {
			hashed = "$2a$" + hashed.substring(4);
		}
		
		return BCrypt.checkpw(password, hashed);
	}

}
