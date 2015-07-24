package ca.uhnresearch.pughlab.tracker.security;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapRealm extends AuthenticatingRealm {
	
    private static final Logger log = LoggerFactory.getLogger(LdapRealm.class);
    
    /**
     * A collection of contexts. Order is not implied here, and we might well
     * end up iterating at random, which is not at all a bad thing if we have 
     * multople servers
     */
    Collection<LdapContext> ldapContexts = new ArrayList<LdapContext>();
    
    private Collection<LdapContext> getLdapContextsForToken(AuthenticationToken token) {
    	Collection<LdapContext> result = new ArrayList<LdapContext>();
    	for(LdapContext context : getLdapContexts()) {
    		if (context.canAuthenticate(token, this)) {
    			result.add(context);
    		}
    	}
    	return result;
    }
    
    private AuthenticationInfo queryContexts(AuthenticationToken token, Collection<LdapContext> contexts) throws AuthenticationException {
    	AuthenticationException exception = null;
    	
    	for(LdapContext context : contexts) {
    		try {
    			return context.query(token, this);
    		} catch (AuthenticationException e) {
    			if (exception == null) {
    				exception = e;
    			}
    		}
    	}
    	
    	throw exception;
    }

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		
		if (token instanceof UsernamePasswordToken) {
			log.debug("Attempting to authenticate: {}", token);
			Collection<LdapContext> contexts = getLdapContextsForToken(token);
			return queryContexts(token, contexts);
		} else {
			throw new AuthenticationException("Invalid token type: " + token.toString());
		}
	}

	/**
	 * @return the ldapContexts
	 */
	public Collection<LdapContext> getLdapContexts() {
		return ldapContexts;
	}

	/**
	 * @param ldapContexts the ldapContexts to set
	 */
	public void setLdapContexts(Collection<LdapContext> ldapContexts) {
		this.ldapContexts = ldapContexts;
	}
}