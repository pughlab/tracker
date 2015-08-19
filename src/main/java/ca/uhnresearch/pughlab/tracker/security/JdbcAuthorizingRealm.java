package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;

public class JdbcAuthorizingRealm extends JdbcRealm {
	
	private AuthorizationRepository authorizationRepository;

	/**
	 * Indicator that authentication is not supported
	 */
	public boolean supports(AuthenticationToken token) { 
	    return false; 
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		AuthorizationInfo result = super.doGetAuthorizationInfo(principals);
		String primary = principals.getPrimaryPrincipal().toString();
		AuditLogRecord record = new AuditLogRecord();
		record.setEventType("login");
		record.setEventUser(primary);
		return result;
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

	/**
	 * @return the authorizationRepository
	 */
	public AuthorizationRepository getAuthorizationRepository() {
		return authorizationRepository;
	}

	/**
	 * @param authorizationRepository the authorizationRepository to set
	 */
	public void setAuthorizationRepository(AuthorizationRepository authorizationRepository) {
		this.authorizationRepository = authorizationRepository;
	}
}
