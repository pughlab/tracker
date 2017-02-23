package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;

public class JdbcAuthorizingRealm extends JdbcRealm {
	
	/**
	 * Indicator that authentication is not supported
	 */
	public boolean supports(AuthenticationToken token) { 
	    return false; 
	}
	
	/**
	 * Overrides default behaviour for identifying the principal, which will always be
	 * the primary principal, not necessarily from this realm
	 */
	@Override
    protected Object getAvailablePrincipal(PrincipalCollection principals) {
        if (!CollectionUtils.isEmpty(principals)) {
        	return principals.getPrimaryPrincipal().toString();
        }
        return null;
    }

	/**
	 * Exposes clearCachedAuthorizationInfo as a public method so that other parts
	 * of the system can dynamically clear cached authorization information. 
	 */
	@Override
    public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }
	
	/**
	 * Overrides default behaviour so that the cache key is always based on 
	 * the primary principal, which is what we use to key from authentication
	 * to authorization. See: https://mail-archives.apache.org/mod_mbox/shiro-user/201305.mbox/%3CCAAtvD4UyoLaeLS0-X+s7HH7YJxPQDXXHUQDFNC=PiiJ31ym8yw@mail.gmail.com%3E
	 */
	@Override
	protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
		return principals.getPrimaryPrincipal();
	}
}
