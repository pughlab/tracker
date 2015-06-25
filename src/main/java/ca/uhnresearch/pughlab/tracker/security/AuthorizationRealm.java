package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class AuthorizationRealm extends AuthorizingRealm {

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection obj) {
		
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.addObjectPermission(new WildcardPermission("study:*:DEMO"));
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken obj) throws AuthenticationException {
		throw new AuthenticationException("Authentication not supported");
	}
	
	@Override
	public boolean supports (AuthenticationToken token) {
	    return false;
	}
}
