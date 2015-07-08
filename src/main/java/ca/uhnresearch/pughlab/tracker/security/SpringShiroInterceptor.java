package ca.uhnresearch.pughlab.tracker.security;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.subject.WebSubject;
import org.apache.shiro.mgt.SecurityManager;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * A slightly modified ShiroInterceptor that accepts an injectable SecurityManager. 
 * This means we don't been a complete environment setup, that we would usually 
 * get through a non-Spring-based configuration. 
 * 
 * @author stuartw
 */
@Configurable
public class SpringShiroInterceptor extends AtmosphereInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(SpringShiroInterceptor.class);
	
	@Inject
	@Named("securityManager")
	private SecurityManager securityManager;
	
	@Override
    public Action inspect(AtmosphereResource r) {

        if (Utils.webSocketMessage(r)) return Action.CONTINUE;

        if (r.getRequest().localAttributes().containsKey(FrameworkConfig.SECURITY_SUBJECT) == false) {
            try {
                Subject currentUser = null;
                if (r.transport().equals(TRANSPORT.WEBSOCKET)) {
                    currentUser = new WebSubject.Builder(getSecurityManager(), r.getRequest(), r.getResponse()).buildWebSubject();
                } else {
                    currentUser = SecurityUtils.getSubject();
                }
                if (currentUser != null) {
                    r.getRequest().setAttribute(FrameworkConfig.SECURITY_SUBJECT, currentUser);
                }
            } catch (UnavailableSecurityManagerException ex) {
                logger.debug("Shiro Web Security : {}", ex.getMessage());
            } catch (java.lang.IllegalStateException ex) {
                logger.debug("Shiro Web Environment : {}", ex.getMessage());
            }
        }

        return Action.CONTINUE;
    }

	/**
	 * @return the securityManager
	 */
	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	/**
	 * @param securityManager the securityManager to set
	 */
	public void setSecurityManager(SecurityManager securityManager) {
		this.securityManager = securityManager;
	}
}
