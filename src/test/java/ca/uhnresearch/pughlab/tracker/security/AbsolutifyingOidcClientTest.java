package ca.uhnresearch.pughlab.tracker.security;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;

import com.nimbusds.openid.connect.sdk.util.Resource;
import com.nimbusds.openid.connect.sdk.util.ResourceRetriever;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;

public class AbsolutifyingOidcClientTest {
	
	AbsolutifyingOidcClient client;
	
	@Before
	public void setUp() {
		client = new AbsolutifyingOidcClient();
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
	public void testRedirectAction() throws MalformedURLException, IOException {
		WebContext context = createMock(WebContext.class);
		context.setSessionAttribute(eq("oidcStateAttribute"), anyObject(Object.class));
		expectLastCall();
		replay(context);
		
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

		Resource resource = createMock(Resource.class);
		expect(resource.getContent()).andReturn(discoveryContent);
		replay(resource);
		
		Resource jwksResource = createMock(Resource.class);
		expect(jwksResource.getContent()).andReturn(jwksContent);
		replay(jwksResource);		
		
		ResourceRetriever resourceRetriever = createMock(ResourceRetriever.class);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/discovery"))).andReturn(resource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andReturn(jwksResource);
		replay(resourceRetriever);
		
		client.setClientID("flooby1234");
		client.setSecret("dhfdsuyfbnjkdsz.ndifszbfzs");
		client.setDiscoveryURI("http://localhost/discovery");
		client.setCallbackUrl("http://example.com/");
		client.setResourceRetriever(resourceRetriever);
		client.internalInit();
		
		RedirectAction action = client.retrieveRedirectAction(context);
		Assert.assertNotNull(action);
	}
}
