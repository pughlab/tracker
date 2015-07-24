package ca.uhnresearch.pughlab.tracker.security;

import java.util.Collection;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;

public class JdbcAuthorizingRealm extends JdbcRealm {

	public boolean supports(AuthenticationToken token) { 
	    return false; 
	}
	
	/**
	 * Overrides default behaviour for identifying the principal, which will always be
	 * the primary principal, not necessarily from this realm
	 */
	@Override
    protected Object getAvailablePrincipal(PrincipalCollection principals) {
        Object primary = null;
        if (!CollectionUtils.isEmpty(principals)) {
        	return principals.getPrimaryPrincipal().toString();
        }
        return primary;
    }

}
