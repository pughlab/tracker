package ca.uhnresearch.pughlab.tracker.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.easymock.EasyMock.*;

public class LdapRealmTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testConstructor() {
		LdapRealm realm = new LdapRealm();
		Assert.assertNotNull(realm);
	}

	@Test
	public void testSetContexts() {
		
		DomainLdapContext context = createMock(DomainLdapContext.class);
		replay(context);
		
		List<LdapContext> contexts = new ArrayList<LdapContext>();
		contexts.add(context);
		
		LdapRealm realm = new LdapRealm();
		realm.setLdapContexts(contexts);
	}

	@Test
	public void testDoGetAuthenticationInfoInvalidToken() {
		
		AuthenticationToken token = createMock(AuthenticationToken.class);
		replay(token);
		
		LdapRealm realm = new LdapRealm();
		
		thrown.expect(AuthenticationException.class);

		AuthenticationInfo result = realm.doGetAuthenticationInfo(token);
		
		Assert.assertNotNull(result);
	}

	@Test
	public void testDoGetAuthenticationInfo() {
		
		LdapRealm realm = new LdapRealm();

		UsernamePasswordToken token = createMock(UsernamePasswordToken.class);
		replay(token);
		
		AuthenticationInfo info = createMock(AuthenticationInfo.class);
		replay(info);
		
		LdapContext context = createMock(LdapContext.class);
		expect(context.canAuthenticate(eq(token), eq(realm))).andStubReturn(true);
		expect(context.query(eq(token), eq(realm))).andStubReturn(info);
		replay(context);
		
		List<LdapContext> contexts = new ArrayList<LdapContext>();
		contexts.add(context);
		
		realm.setLdapContexts(contexts);
		
		AuthenticationInfo result = realm.doGetAuthenticationInfo(token);
		Assert.assertNotNull(result);
		Assert.assertEquals(result, info);		
	}
}
