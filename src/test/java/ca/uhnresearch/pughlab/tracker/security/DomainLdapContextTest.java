package ca.uhnresearch.pughlab.tracker.security;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.anyObject;

public class DomainLdapContextTest {
	
	Realm realm;
	
	@Before
	public void setUp() {
		realm = createMock(Realm.class);
		expect(realm.getName()).andStubReturn("mockrealm");
		replay(realm);
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testConstructor() {
		DomainLdapContext context = new DomainLdapContext();
		Assert.assertNotNull(context);
	}

	@Test
	public void testGetSetLdapHost() {
		DomainLdapContext context = new DomainLdapContext();
		context.setLdapHost("ad.example.com");
		Assert.assertEquals("ad.example.com", context.getLdapHost());
	}

	@Test
	public void testGetSetDomain() {
		DomainLdapContext context = new DomainLdapContext();
		context.setDomain("example.com");
		Assert.assertEquals("example.com", context.getDomain());
	}

	@Test
	public void testGetSetLdapPort() {
		DomainLdapContext context = new DomainLdapContext();
		context.setLdapPort(8389);
		Assert.assertEquals(8389, context.getLdapPort());
	}

	@Test
	public void testGetSetTimeout() {
		DomainLdapContext context = new DomainLdapContext();
		context.setTimeout(120);
		Assert.assertEquals(120, context.getTimeout());
	}

	@Test
	public void testGetSetSearchTemplate() {
		DomainLdapContext context = new DomainLdapContext();
		context.setSearchTemplate("OU=People,DC=example,DC=com");
		Assert.assertEquals("OU=People,DC=example,DC=com", context.getSearchTemplate());
	}

	@Test
	public void testGetSetFilterTemplate() {
		DomainLdapContext context = new DomainLdapContext();
		context.setFilterTemplate("(userPrincipalName={0})");
		Assert.assertEquals("(userPrincipalName={0})", context.getFilterTemplate());
	}

	@Test
	public void testGetSetMinEvictableIdleTimeMillis() {
		DomainLdapContext context = new DomainLdapContext();
		context.setMinEvictableIdleTimeMillis(12345);
		Assert.assertEquals(12345, context.getMinEvictableIdleTimeMillis());
	}

	@Test
	public void testGetSetTimeBetweenEvictionRunsMillis() {
		DomainLdapContext context = new DomainLdapContext();
		context.setTimeBetweenEvictionRunsMillis(54321);
		Assert.assertEquals(54321, context.getTimeBetweenEvictionRunsMillis());
	}
	
	@Test
	public void testGetSetDisplayNameAttribute() {
		DomainLdapContext context = new DomainLdapContext();
		context.setDisplayNameAttribute("displayNameTest");
		Assert.assertEquals("displayNameTest", context.getDisplayNameAttribute());
	}

	@Test
	public void testGetSetEmailAttribute() {
		DomainLdapContext context = new DomainLdapContext();
		context.setEmailAttribute("emailTest");
		Assert.assertEquals("emailTest", context.getEmailAttribute());
	}

	@Test 
	public void testQuerySuccess() throws Exception {
		
		LdapResult bindResult = createMock(LdapResult.class);
		expect(bindResult.getResultCode()).andStubReturn(ResultCodeEnum.SUCCESS);
		replay(bindResult);
		
		BindResponse bindResponse = createMock(BindResponse.class);
		expect(bindResponse.getLdapResult()).andStubReturn(bindResult);
		replay(bindResponse);
		
		Entry resultEntry = createMock(Entry.class);
		expect(resultEntry.get("displayName")).andStubReturn(new DefaultAttribute("displayName", "Stuart Watt"));
		expect(resultEntry.get("mail")).andStubReturn(new DefaultAttribute("mail", "stuart@morungos.com"));
		replay(resultEntry);
		
		EntryCursor cursor = createMock(EntryCursor.class);
		expect(cursor.next()).andReturn(true);
		expect(cursor.get()).andReturn(resultEntry);
		replay(cursor);
		
		LdapConnection connection = createMock(LdapConnection.class);
		expect(connection.bind(anyObject(BindRequest.class))).andStubReturn(bindResponse);
		expect(connection.isAuthenticated()).andStubReturn(true);
		expect(connection.search("OU=People,DC=example,DC=com", "(userPrincipalName=stuart@example.com)", SearchScope.SUBTREE)).andStubReturn(cursor);
		replay(connection);
		
		LdapConnectionPool pool = createMock(LdapConnectionPool.class);
		expect(pool.getConnection()).andStubReturn(connection);
		pool.releaseConnection(connection);
		expectLastCall();
		replay(pool);
		
		DomainLdapContext context = EasyMock.createMockBuilder(DomainLdapContext.class)
				.addMockedMethod("getConnectionPool")
				.withConstructor()
				.createMock();
		expect(context.getConnectionPool()).andStubReturn(pool);
		replay(context);
		
		context.setSearchTemplate("OU=People,DC=example,DC=com");
		context.setFilterTemplate("(userPrincipalName={0})");
		context.setDomain("example.com");
		
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername("stuart");
		token.setPassword("password".toCharArray());
		
		AuthenticationInfo info = context.query(token, realm);
		Assert.assertNotNull(info);
		Assert.assertEquals(2, info.getPrincipals().asList().size());
		Assert.assertEquals("stuart@example.com", info.getPrincipals().getPrimaryPrincipal());
	}
	
	@Test 
	public void testQueryNotAuthenticated() throws Exception {
		
		LdapResult bindResult = createMock(LdapResult.class);
		expect(bindResult.getResultCode()).andStubReturn(ResultCodeEnum.SUCCESS);
		replay(bindResult);
		
		BindResponse bindResponse = createMock(BindResponse.class);
		expect(bindResponse.getLdapResult()).andStubReturn(bindResult);
		replay(bindResponse);

		LdapConnection connection = createMock(LdapConnection.class);
		expect(connection.bind(anyObject(BindRequest.class))).andStubReturn(bindResponse);
		expect(connection.isAuthenticated()).andStubReturn(false);
		replay(connection);

		LdapConnectionPool pool = createMock(LdapConnectionPool.class);
		expect(pool.getConnection()).andStubReturn(connection);
		pool.releaseConnection(connection);
		expectLastCall();
		replay(pool);

		DomainLdapContext context = EasyMock.createMockBuilder(DomainLdapContext.class)
				.addMockedMethod("getConnectionPool")
				.withConstructor()
				.createMock();
		expect(context.getConnectionPool()).andStubReturn(pool);
		replay(context);

		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername("stuart");
		token.setPassword("password".toCharArray());

		thrown.expect(AuthenticationException.class);
		thrown.expectMessage("Failed to authenticate");

		context.query(token, realm);
	}
	
	@Test 
	public void testQueryFailure() throws Exception {

		LdapResult bindResult = createMock(LdapResult.class);
		expect(bindResult.getResultCode()).andStubReturn(ResultCodeEnum.UNAVAILABLE);
		expect(bindResult.getDiagnosticMessage()).andReturn("It went wrong");
		replay(bindResult);

		BindResponse bindResponse = createMock(BindResponse.class);
		expect(bindResponse.getLdapResult()).andStubReturn(bindResult);
		replay(bindResponse);

		LdapConnection connection = createMock(LdapConnection.class);
		expect(connection.bind(anyObject(BindRequest.class))).andStubReturn(bindResponse);
		replay(connection);
		
		LdapConnectionPool pool = createMock(LdapConnectionPool.class);
		expect(pool.getConnection()).andStubReturn(connection);
		pool.releaseConnection(connection);
		expectLastCall();
		replay(pool);
		
		DomainLdapContext context = EasyMock.createMockBuilder(DomainLdapContext.class).addMockedMethod("getConnectionPool").createMock();
		expect(context.getConnectionPool()).andStubReturn(pool);
		replay(context);
		
		context.setSearchTemplate("OU=People,DC=example,DC=com");
		context.setFilterTemplate("(userPrincipalName={0})");
		context.setDomain("example.com");
		
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername("stuart");
		token.setPassword("password".toCharArray());
		
		thrown.expect(AuthenticationException.class);
		thrown.expectMessage("It went wrong");

		context.query(token, realm);
	}
	
	@Test 
	public void testCanAuthenticateMatchingDomain() throws Exception {
		AuthenticationToken token = createMock(AuthenticationToken.class);
		expect(token.getPrincipal()).andReturn("stuart@example.com");
		replay(token);
		
		DomainLdapContext context = new DomainLdapContext();
		context.setDomain("example.com");
		
		Assert.assertTrue(context.canAuthenticate(token, realm));
	}

	@Test 
	public void testCantAuthenticateOtherDomain() throws Exception {
		AuthenticationToken token = createMock(AuthenticationToken.class);
		expect(token.getPrincipal()).andReturn("stuart@example.com");
		replay(token);
		
		DomainLdapContext context = new DomainLdapContext();
		context.setDomain("morungos.com");
		
		Assert.assertFalse(context.canAuthenticate(token, realm));
	}

	@Test 
	public void testCantAuthenticateWithoutDomain() throws Exception {
		AuthenticationToken token = createMock(AuthenticationToken.class);
		expect(token.getPrincipal()).andReturn("stuart");
		replay(token);
		
		DomainLdapContext context = new DomainLdapContext();
		context.setDomain("morungos.com");
		
		Assert.assertTrue(context.canAuthenticate(token, realm));
	}
	
	@Test
	public void testGetConnectionsPool() throws Exception {
		DomainLdapContext context = new DomainLdapContext();
		
		LdapConnectionPool pool = context.getConnectionPool();
		Assert.assertNotNull(pool);
	}
	

	@Test
	public void testGetConnectionsPoolReuse() throws Exception {
		DomainLdapContext context = new DomainLdapContext();
		
		LdapConnectionPool pool1 = context.getConnectionPool();
		LdapConnectionPool pool2 = context.getConnectionPool();
		Assert.assertNotNull(pool2);
		Assert.assertEquals(pool1,  pool2);
	}
}
