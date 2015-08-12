package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class JdbcAuthenticatingRealm extends JdbcRealm {
	
	public JdbcAuthenticatingRealm() {
		super();
		setPermissionsLookupEnabled(true);
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		return new SimpleAuthorizationInfo();
	}
}
