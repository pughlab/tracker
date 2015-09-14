package ca.uhnresearch.pughlab.tracker.security;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Mechanism;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.oidc.profile.OidcProfile;

import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.util.Resource;
import com.nimbusds.openid.connect.sdk.util.ResourceRetriever;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.containsString;

public class AbsolutifyingOidcClientTest {
	
	AbsolutifyingOidcClient client;
	
	Resource discoveryResource;
	Resource jwksResource;
	ResourceRetriever resourceRetriever;

	String discoveryContent = 
			"{\"subject_types_supported\":[\"public\"]" +
	        ",\"issuer\":\"http://localhost\"" +
	        ",\"jwks_uri\":\"http://localhost/jwks\"" +
	        ",\"authorization_endpoint\":\"http://localhost/authorize\"" +
			"}";
	
	String jwksContent = 
			"{\"keys\":[{" +
	        "  \"kty\":\"RSA\"," +
			"  \"alg\":\"RS256\"," +
	        "  \"kid\":\"2011-04-29\"," +
			"  \"e\":\"AQAB\"," +
	        "  \"n\": \"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\"" +
			"}]}";

	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws MalformedURLException, IOException {
		client = new AbsolutifyingOidcClient();
		
		discoveryResource = createMock(Resource.class);
		expect(discoveryResource.getContent()).andReturn(discoveryContent);
		replay(discoveryResource);

		jwksResource = createMock(Resource.class);
		expect(jwksResource.getContent()).andReturn(jwksContent);
		replay(jwksResource);		
		
		resourceRetriever = createMock(ResourceRetriever.class);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/discovery"))).andReturn(discoveryResource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andReturn(jwksResource);
		replay(resourceRetriever);
		
		client.setClientID("flooby1234");
		client.setSecret("dhfdsuyfbnjkdsz.ndifszbfzs");
		client.setDiscoveryURI("http://localhost/discovery");
		client.setCallbackUrl("http://example.com/");
		client.setResourceRetriever(resourceRetriever);
	}

	@Test
	public void testGetSetClientID() {
		client.setClientID("flooby1234");
	}

	@Test
	public void testGetSetSecret() {
		client.setSecret("dhfdsuyfbnjkdsz.ndifszbfzs");
	}
	
	@Test
	public void testGetMechanism() {
		Assert.assertEquals(Mechanism.OPENID_CONNECT_PROTOCOL, client.getMechanism());
	}
	
	@Test 
	public void testNewClient() {
		client.internalInit();

		BaseClient<ContextualOidcCredentials, OidcProfile> newClient = client.newClient();
		Assert.assertNotNull(newClient);
	}
	
	@Test
	public void testRedirectAction() throws MalformedURLException, IOException {
		WebContext context = createMock(WebContext.class);
		context.setSessionAttribute(eq("oidcStateAttribute"), anyObject(Object.class));
		expectLastCall();
		replay(context);
		
		client.internalInit();
		
		RedirectAction action = client.retrieveRedirectAction(context);
		Assert.assertNotNull(action);
	}
	
	@Test
	public void testRedirectActionWithNonce() throws MalformedURLException, IOException {
		WebContext context = createMock(WebContext.class);
		context.setSessionAttribute(eq("oidcStateAttribute"), anyObject(Object.class));
		context.setSessionAttribute(eq("oidcNonceAttribute"), anyObject(Object.class));
		expectLastCall();
		replay(context);
		
		client.addCustomParam("useNonce", "true");
		client.internalInit();
		
		RedirectAction action = client.retrieveRedirectAction(context);
		Assert.assertNotNull(action);
	}
	
	@Test
	public void testGetAbsoluteUriWithoutScheme() throws URISyntaxException {
		
		WebContext context = createMock(WebContext.class);
		expect(context.getScheme()).andReturn("http");
		expect(context.getServerName()).andReturn("google.com");
		expect(context.getServerPort()).andReturn(1234);
		replay(context);
		
		URI result = client.getAbsoluteUri(new URI("/"), context);
		Assert.assertEquals("http://google.com:1234/", result.toString());
	}
	
	@Test
	public void testGetAbsoluteUriWithScheme() throws URISyntaxException {
		
		WebContext context = createMock(WebContext.class);
		replay(context);
		
		URI result = client.getAbsoluteUri(new URI("https://google.ca/"), context);
		Assert.assertEquals("https://google.ca/", result.toString());
	}
	
	@Test
	public void testRetrieveCredentialsInvalidState() throws RequiresHttpAction, MalformedURLException, IOException {
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("state", new String[] { "value" });
		
		WebContext context = createMock(WebContext.class);
		expect(context.getRequestParameters()).andReturn(parameters);
		expect(context.getSessionAttribute("oidcStateAttribute")).andStubReturn(new State("nibble"));
		replay(context);

		client.internalInit();

		thrown.expect(TechnicalException.class);
		thrown.expectMessage(containsString("State parameter is different"));

		client.retrieveCredentials(context);
	}

	@Test
	public void testRetrieveCredentialsValidState() throws RequiresHttpAction, MalformedURLException, IOException {
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("state", new String[] { "nibble" });
		parameters.put("code", new String[] { "1234" });
		
		WebContext context = createMock(WebContext.class);
		expect(context.getRequestParameters()).andReturn(parameters);
		expect(context.getSessionAttribute("oidcStateAttribute")).andStubReturn(new State("nibble"));
		replay(context);

		client.internalInit();

		ContextualOidcCredentials credentials = client.retrieveCredentials(context);
		Assert.assertEquals("1234", credentials.getCode().getValue());
	}
}
