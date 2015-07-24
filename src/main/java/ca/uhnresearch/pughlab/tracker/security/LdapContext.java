package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.Realm;

public interface LdapContext {
	public AuthenticationInfo query(AuthenticationToken token, Realm realm) throws AuthenticationException; 
	
	public boolean canAuthenticate(AuthenticationToken token, Realm realm);
}
