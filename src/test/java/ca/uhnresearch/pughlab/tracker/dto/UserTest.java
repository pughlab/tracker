package ca.uhnresearch.pughlab.tracker.dto;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.oidc.profile.OidcProfile;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;

import ca.uhnresearch.pughlab.tracker.security.LdapProfile;
import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;

public class UserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testStringConstructor() {
		
		User result = new User("stuart");
		Assert.assertEquals("stuart", result.getUsername());
		Assert.assertEquals(false, result.getAdministrator());
		Assert.assertNull(result.getDisplayName());
		Assert.assertNull(result.getEmail());
	}
	

	@Test
	public void testBasicConstructor() {
		
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		principals.add("stuart", "mock");

		Subject subject = createMock(Subject.class);
		expect(subject.getPrincipals()).andStubReturn(principals);
		expect(subject.hasRole("ROLE_ADMIN")).andReturn(false);
		replay(subject);
		
		User result = new User(subject);
		Assert.assertEquals("stuart", result.getUsername());
		Assert.assertEquals(false, result.getAdministrator());
		Assert.assertNull(result.getDisplayName());
		Assert.assertNull(result.getEmail());
	}
	
	@Test
	public void testAdminConstructor() {
		
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		principals.add("stuart", "mock");

		Subject subject = createMock(Subject.class);
		expect(subject.getPrincipals()).andStubReturn(principals);
		expect(subject.hasRole("ROLE_ADMIN")).andReturn(true);
		replay(subject);
		
		User result = new User(subject);
		Assert.assertEquals("stuart", result.getUsername());
		Assert.assertEquals(true, result.getAdministrator());
	}
	
	@Test
	public void testLdapConstructor() {
		
		LdapProfile profile = new LdapProfile("stuart");
		profile.setDisplayName("Stuart Watt");
		profile.setEmail("stuart@morungos.com");
		
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		principals.add(profile, "mock");

		Subject subject = createMock(Subject.class);
		expect(subject.getPrincipals()).andStubReturn(principals);
		expect(subject.hasRole("ROLE_ADMIN")).andReturn(false);
		replay(subject);
		
		User result = new User(subject);
		Assert.assertEquals("stuart", result.getUsername());
		Assert.assertEquals("Stuart Watt", result.getDisplayName());
		Assert.assertEquals("stuart@morungos.com", result.getEmail());
	}
	
	@Test
	public void testOidcConstructor() {
		
		BearerAccessToken token = new BearerAccessToken();
		
		OidcProfile profile = new OidcProfile(token);
		profile.addAttribute("preferred_username", "stuart");
		profile.addAttribute("name", "Stuart Watt");
		profile.addAttribute("email", "stuart@morungos.com");
		
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		principals.add(profile, "mock");

		Subject subject = createMock(Subject.class);
		expect(subject.getPrincipals()).andStubReturn(principals);
		expect(subject.hasRole("ROLE_ADMIN")).andReturn(false);
		replay(subject);
		
		User result = new User(subject);
		Assert.assertEquals("stuart", result.getUsername());
		Assert.assertEquals("Stuart Watt", result.getDisplayName());
		Assert.assertEquals("stuart@morungos.com", result.getEmail());
	}
	
	@Test
	public void testInvalidPrincipal() {
		
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		principals.add(new Boolean(true), "mock");

		Subject subject = createMock(Subject.class);
		expect(subject.getPrincipals()).andStubReturn(principals);
		expect(subject.hasRole("ROLE_ADMIN")).andReturn(false);
		replay(subject);
		
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Unexpected principal type"));

		new User(subject);

	}
}
