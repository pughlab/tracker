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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.Request;
import com.nimbusds.oauth2.sdk.http.CommonContentTypes;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.OIDCAccessTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
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
	        ",\"userinfo_endpoint\":\"http://localhost/userinfo\"" +
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
		expect(discoveryResource.getContent()).andStubReturn(discoveryContent);
		replay(discoveryResource);

		jwksResource = createMock(Resource.class);
		expect(jwksResource.getContent()).andStubReturn(jwksContent);
		replay(jwksResource);		
		
		resourceRetriever = createMock(ResourceRetriever.class);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/discovery"))).andStubReturn(discoveryResource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andStubReturn(jwksResource);
		replay(resourceRetriever);
		
		initializeClient(client, resourceRetriever);
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
	public void testIsDirectRedirection() {
		Assert.assertEquals(true, client.isDirectRedirection());
	}
	
	@Test
	public void testECKey() throws MalformedURLException, IOException {
		String ecJwksContent = 
				"{\"keys\":[{" +
		        "  \"kty\":\"EC\"," +
		        "  \"use\":\"sig\"," +
		        "  \"crv\":\"P-256\"," +
				"  \"alg\":\"EC\"," +
		        "  \"kid\":\"2011-04-29\"," +
				"  \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\"," +
		        "  \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\"" +
				"}]}";

		jwksResource = createMock(Resource.class);
		expect(jwksResource.getContent()).andStubReturn(ecJwksContent);
		replay(jwksResource);		
		
		resourceRetriever = createMock(ResourceRetriever.class);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/discovery"))).andStubReturn(discoveryResource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andStubReturn(jwksResource);
		replay(resourceRetriever);
		
		initializeClient(client, resourceRetriever);

		client.internalInit();
	}

	
	@Test
	public void testInvalidKey() throws MalformedURLException, IOException {
		String ecJwksContent = 
				"{\"keys\":[{" +
		        "  \"use\":\"sig\"," +
		        "  \"crv\":\"P-256\"," +
				"  \"alg\":\"EC\"," +
		        "  \"kid\":\"2011-04-29\"," +
				"  \"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\"," +
		        "  \"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\"" +
				"}]}";

		jwksResource = createMock(Resource.class);
		expect(jwksResource.getContent()).andStubReturn(ecJwksContent);
		replay(jwksResource);		
		
		resourceRetriever = createMock(ResourceRetriever.class);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/discovery"))).andStubReturn(discoveryResource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andStubReturn(jwksResource);
		replay(resourceRetriever);
		
		thrown.expect(TechnicalException.class);
		thrown.expectMessage(containsString("Invalid JWK"));

		initializeClient(client, resourceRetriever);
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
	
	@Test
	public void testGetUserAccessTokenResponse() throws Exception {
		
		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getHTTPResponse").createMock();
		
		String jsonResponse = "{\"id\":\"b08e\", \"token_type\":\"bearer\", \"access_token\":\"1234\"}";
		
		HTTPResponse response = createMock(HTTPResponse.class);
		expect(response.getStatusCode()).andStubReturn(200);
		expect(response.getContent()).andStubReturn(jsonResponse);
		expect(response.getContentAsJSONObject()).andStubReturn(JSONObjectUtils.parseJSONObject(jsonResponse));
		response.ensureStatusCode(eq(200));
		expectLastCall().asStub();
		replay(response);
	
		expect(partialMock.getHTTPResponse(anyObject(Request.class))).andStubReturn(response);
		replay(partialMock);
		
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		parameters.put("state", new String[] { "nibble" });
		parameters.put("code", new String[] { "1234" });
		
		WebContext context = createMock(WebContext.class);
		expect(context.getRequestParameters()).andReturn(parameters);
		expect(context.getSessionAttribute("oidcStateAttribute")).andStubReturn(new State("nibble"));
		replay(context);
		
		ContextualOidcCredentials credentials = createMock(ContextualOidcCredentials.class);
		expect(credentials.getCode()).andStubReturn(new AuthorizationCode("1234"));
		expect(credentials.getContext()).andStubReturn(context);
		replay(credentials);
		
		initializeClient(partialMock, resourceRetriever);

		OIDCAccessTokenResponse tokenResponse = partialMock.getUserAccessTokenResponse(credentials);
		Assert.assertEquals("1234", tokenResponse.getAccessToken().toString());
	}
	
	@Test
	public void testGetUserInfo() throws Exception {
		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getHTTPResponse").createMock();
		
		String jsonResponse = "{\"sub\": \"248289761001\", \"name\": \"Jane Doe\", \"given_name\": \"Jane\", \"family_name\": \"Doe\", \"email\": \"janedoe@example.com\", \"preferred_username\": \"j.doe\"}";
		
		HTTPResponse response = createMock(HTTPResponse.class);
		expect(response.getStatusCode()).andStubReturn(200);
		expect(response.getContent()).andStubReturn(jsonResponse);
		expect(response.getContentAsJSONObject()).andStubReturn(JSONObjectUtils.parseJSONObject(jsonResponse));
		expect(response.getContentType()).andStubReturn(CommonContentTypes.APPLICATION_JSON);
		response.ensureStatusCode(eq(200));
		expectLastCall().asStub();
		response.ensureContentType();
		expectLastCall().asStub();
		replay(response);
	
		expect(partialMock.getHTTPResponse(anyObject(Request.class))).andStubReturn(response);
		replay(partialMock);

		BearerAccessToken accessToken = new BearerAccessToken("1234");
				
		initializeClient(partialMock, resourceRetriever);

		UserInfo info = partialMock.getUserInfo(accessToken);
		Assert.assertNotNull(info);
		Assert.assertEquals("Jane", info.getGivenName());
		Assert.assertEquals("Doe", info.getFamilyName());
		Assert.assertEquals("janedoe@example.com", info.getEmail().toString());
	}
	
	
	@Test
	public void testGetUserInfoNoEndpoint() throws Exception {
		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getHTTPResponse").createMock();
		
		String modifiedDiscoveryContent = 
				"{\"subject_types_supported\":[\"public\"]" +
		        ",\"issuer\":\"http://localhost\"" +
		        ",\"jwks_uri\":\"http://localhost/jwks\"" +
		        ",\"authorization_endpoint\":\"http://localhost/authorize\"" +
				"}";

		Resource modifiedDiscoveryResource = createMock(Resource.class);
		expect(modifiedDiscoveryResource.getContent()).andStubReturn(modifiedDiscoveryContent);
		replay(modifiedDiscoveryResource);

		resourceRetriever = createMock(ResourceRetriever.class);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/discovery"))).andStubReturn(modifiedDiscoveryResource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andStubReturn(jwksResource);
		replay(resourceRetriever);

		initializeClient(partialMock, resourceRetriever);

		String jsonResponse = "{\"sub\": \"248289761001\", \"name\": \"Jane Doe\", \"given_name\": \"Jane\", \"family_name\": \"Doe\", \"email\": \"janedoe@example.com\", \"preferred_username\": \"j.doe\"}";

		HTTPResponse response = createMock(HTTPResponse.class);
		expect(response.getStatusCode()).andStubReturn(200);
		expect(response.getContent()).andStubReturn(jsonResponse);
		replay(response);

		BearerAccessToken accessToken = new BearerAccessToken("1234");

		UserInfo info = partialMock.getUserInfo(accessToken);
		Assert.assertNull(info);

	}
	
	
	@Test
	public void testGetClaimsSetFoundKey() throws Exception {
		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getHTTPResponse").createMock();
		
		initializeClient(partialMock, resourceRetriever);
		
		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		header.setKeyID("2011-04-29");

		ReadOnlyJWTClaimsSet claimsSet = createMock(ReadOnlyJWTClaimsSet.class);
		replay(claimsSet);

		SignedJWT jwt = createMock(SignedJWT.class);
		expect(jwt.getHeader()).andStubReturn(header);
		expect(jwt.verify(anyObject(JWSVerifier.class))).andStubReturn(true);
		expect(jwt.getJWTClaimsSet()).andStubReturn(claimsSet);
		replay(jwt);

		ReadOnlyJWTClaimsSet claims = partialMock.getClaimsSet(jwt);
		Assert.assertNotNull(claims);
	}
		
	@Test
	public void testGetClaimsSetMissingKey() throws Exception {
		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getHTTPResponse").createMock();
		
		initializeClient(partialMock, resourceRetriever);
		
		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		header.setKeyID("2011-04-30");

		ReadOnlyJWTClaimsSet claimsSet = createMock(ReadOnlyJWTClaimsSet.class);
		replay(claimsSet);

		SignedJWT jwt = createMock(SignedJWT.class);
		expect(jwt.getHeader()).andStubReturn(header);
		expect(jwt.verify(anyObject(JWSVerifier.class))).andStubReturn(true);
		expect(jwt.getJWTClaimsSet()).andStubReturn(claimsSet);
		replay(jwt);
		
		thrown.expect(MissingKeyException.class);
		partialMock.getClaimsSet(jwt);
	}
		
	@Test
	public void testGetClaimsSetMissingKeyRequest() throws Exception {
		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getHTTPResponse").createMock();
		
		
		String secondJwksContent = 
				"{\"keys\":[{" +
		        "  \"kty\":\"RSA\"," +
				"  \"alg\":\"RS256\"," +
		        "  \"kid\":\"2011-04-30\"," +
				"  \"e\":\"AQAB\"," +
		        "  \"n\": \"0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMstn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbISD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqbw0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw\"" +
				"}]}";
		
		Resource secondJwksResource = createMock(Resource.class);
		expect(secondJwksResource.getContent()).andStubReturn(secondJwksContent);
		replay(secondJwksResource);		

		ResourceRetriever resourceRetriever = createMock(ResourceRetriever.class);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/discovery"))).andStubReturn(discoveryResource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andReturn(jwksResource);
		expect(resourceRetriever.retrieveResource(new URL("http://localhost/jwks"))).andReturn(secondJwksResource);
		replay(resourceRetriever);
		
		initializeClient(partialMock, resourceRetriever);

		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		header.setKeyID("2011-04-30");

		ReadOnlyJWTClaimsSet claimsSet = createMock(ReadOnlyJWTClaimsSet.class);
		replay(claimsSet);

		SignedJWT jwt = createMock(SignedJWT.class);
		expect(jwt.getHeader()).andStubReturn(header);
		expect(jwt.verify(anyObject(JWSVerifier.class))).andStubReturn(true);
		expect(jwt.getJWTClaimsSet()).andStubReturn(claimsSet);
		replay(jwt);
		
		ReadOnlyJWTClaimsSet claims = partialMock.getClaimsSet(jwt);
		Assert.assertNotNull(claims);
	}
		
	@Test
	public void testRetrieveUserProfile() throws Exception {

		SignedJWT idToken = createMock(SignedJWT.class);
		replay(idToken);

		OIDCAccessTokenResponse accessTokenResponse = createMock(OIDCAccessTokenResponse.class);
		expect(accessTokenResponse.getAccessToken()).andStubReturn(new BearerAccessToken("1234"));
		expect(accessTokenResponse.getIDToken()).andStubReturn(idToken);
		replay(accessTokenResponse);
		
		JWTClaimsSet jwtClaims = createMock(JWTClaimsSet.class);
		expect(jwtClaims.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		replay(jwtClaims);
		
		UserInfo userInfo = createMock(UserInfo.class);
		expect(userInfo.toJWTClaimsSet()).andStubReturn(jwtClaims);
		replay(userInfo);
		
		ReadOnlyJWTClaimsSet claimsSet = createMock(ReadOnlyJWTClaimsSet.class);
		expect(claimsSet.getSubject()).andStubReturn("248289761001");
		expect(claimsSet.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		replay(claimsSet);
		
		WebContext context = createMock(WebContext.class);
		expect(context.getSessionAttribute(eq("oidcNonceAttribute"))).andStubReturn("NONCE");
		replay(context);
		
		ContextualOidcCredentials credentials = createMock(ContextualOidcCredentials.class);
		expect(credentials.getContext()).andStubReturn(context);
		replay(credentials);
		
		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getUserAccessTokenResponse")
				.addMockedMethod("getUserInfo")
				.addMockedMethod("getClaimsSet")
				.createMock();

		expect(partialMock.getUserAccessTokenResponse(eq(credentials))).andStubReturn(accessTokenResponse);
		expect(partialMock.getUserInfo(anyObject(BearerAccessToken.class))).andStubReturn(userInfo);
		expect(partialMock.getClaimsSet(eq(idToken))).andStubReturn(claimsSet);
		replay(partialMock);

		initializeClient(partialMock, resourceRetriever);

		OidcProfile retrieveUserProfile = partialMock.retrieveUserProfile(credentials, context);
		Assert.assertNotNull(retrieveUserProfile);
	}
	
	@Test
	public void testRetrieveUserProfileWithNonce() throws Exception {

		SignedJWT idToken = createMock(SignedJWT.class);
		replay(idToken);

		OIDCAccessTokenResponse accessTokenResponse = createMock(OIDCAccessTokenResponse.class);
		expect(accessTokenResponse.getAccessToken()).andStubReturn(new BearerAccessToken("1234"));
		expect(accessTokenResponse.getIDToken()).andStubReturn(idToken);
		replay(accessTokenResponse);
		
		JWTClaimsSet jwtClaims = createMock(JWTClaimsSet.class);
		expect(jwtClaims.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		replay(jwtClaims);
		
		UserInfo userInfo = createMock(UserInfo.class);
		expect(userInfo.toJWTClaimsSet()).andStubReturn(jwtClaims);
		replay(userInfo);
		
		ReadOnlyJWTClaimsSet claimsSet = createMock(ReadOnlyJWTClaimsSet.class);
		expect(claimsSet.getSubject()).andStubReturn("248289761001");
		expect(claimsSet.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		expect(claimsSet.getStringClaim("nonce")).andStubReturn("NONCE");
		replay(claimsSet);
		
		WebContext context = createMock(WebContext.class);
		expect(context.getSessionAttribute(eq("oidcNonceAttribute"))).andStubReturn("NONCE");
		replay(context);
		
		ContextualOidcCredentials credentials = createMock(ContextualOidcCredentials.class);
		expect(credentials.getContext()).andStubReturn(context);
		replay(credentials);

		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getUserAccessTokenResponse")
				.addMockedMethod("getUserInfo")
				.addMockedMethod("getClaimsSet")
				.createMock();

		expect(partialMock.getUserAccessTokenResponse(eq(credentials))).andStubReturn(accessTokenResponse);
		expect(partialMock.getUserInfo(anyObject(BearerAccessToken.class))).andStubReturn(userInfo);
		expect(partialMock.getClaimsSet(eq(idToken))).andStubReturn(claimsSet);
		replay(partialMock);

		partialMock.addCustomParam("useNonce", "true");
		initializeClient(partialMock, resourceRetriever);

		OidcProfile retrieveUserProfile = partialMock.retrieveUserProfile(credentials, context);
		Assert.assertNotNull(retrieveUserProfile);
	}
	
	@Test
	public void testRetrieveUserProfileWithNullNonce() throws Exception {

		SignedJWT idToken = createMock(SignedJWT.class);
		replay(idToken);

		OIDCAccessTokenResponse accessTokenResponse = createMock(OIDCAccessTokenResponse.class);
		expect(accessTokenResponse.getAccessToken()).andStubReturn(new BearerAccessToken("1234"));
		expect(accessTokenResponse.getIDToken()).andStubReturn(idToken);
		replay(accessTokenResponse);
		
		JWTClaimsSet jwtClaims = createMock(JWTClaimsSet.class);
		expect(jwtClaims.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		replay(jwtClaims);
		
		UserInfo userInfo = createMock(UserInfo.class);
		expect(userInfo.toJWTClaimsSet()).andStubReturn(jwtClaims);
		replay(userInfo);
		
		ReadOnlyJWTClaimsSet claimsSet = createMock(ReadOnlyJWTClaimsSet.class);
		expect(claimsSet.getSubject()).andStubReturn("248289761001");
		expect(claimsSet.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		expect(claimsSet.getStringClaim("nonce")).andStubReturn(null);
		replay(claimsSet);
		
		WebContext context = createMock(WebContext.class);
		expect(context.getSessionAttribute(eq("oidcNonceAttribute"))).andStubReturn("NONCE");
		replay(context);
		
		ContextualOidcCredentials credentials = createMock(ContextualOidcCredentials.class);
		expect(credentials.getContext()).andStubReturn(context);
		replay(credentials);

		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getUserAccessTokenResponse")
				.addMockedMethod("getUserInfo")
				.addMockedMethod("getClaimsSet")
				.createMock();

		expect(partialMock.getUserAccessTokenResponse(eq(credentials))).andStubReturn(accessTokenResponse);
		expect(partialMock.getUserInfo(anyObject(BearerAccessToken.class))).andStubReturn(userInfo);
		expect(partialMock.getClaimsSet(eq(idToken))).andStubReturn(claimsSet);
		replay(partialMock);

		partialMock.addCustomParam("useNonce", "true");
		initializeClient(partialMock, resourceRetriever);
		
		thrown.expect(TechnicalException.class);
		thrown.expectMessage(containsString("possible threat of cross-site request forgery"));

		partialMock.retrieveUserProfile(credentials, context);
	}
	
	@Test
	public void testRetrieveUserProfileWithIncorrectNonce() throws Exception {

		SignedJWT idToken = createMock(SignedJWT.class);
		replay(idToken);

		OIDCAccessTokenResponse accessTokenResponse = createMock(OIDCAccessTokenResponse.class);
		expect(accessTokenResponse.getAccessToken()).andStubReturn(new BearerAccessToken("1234"));
		expect(accessTokenResponse.getIDToken()).andStubReturn(idToken);
		replay(accessTokenResponse);
		
		JWTClaimsSet jwtClaims = createMock(JWTClaimsSet.class);
		expect(jwtClaims.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		replay(jwtClaims);
		
		UserInfo userInfo = createMock(UserInfo.class);
		expect(userInfo.toJWTClaimsSet()).andStubReturn(jwtClaims);
		replay(userInfo);
		
		ReadOnlyJWTClaimsSet claimsSet = createMock(ReadOnlyJWTClaimsSet.class);
		expect(claimsSet.getSubject()).andStubReturn("248289761001");
		expect(claimsSet.getAllClaims()).andStubReturn(new HashMap<String, Object>());
		expect(claimsSet.getStringClaim("nonce")).andStubReturn("NONCE");
		replay(claimsSet);
		
		WebContext context = createMock(WebContext.class);
		expect(context.getSessionAttribute(eq("oidcNonceAttribute"))).andStubReturn("NONCE2");
		replay(context);
		
		ContextualOidcCredentials credentials = createMock(ContextualOidcCredentials.class);
		expect(credentials.getContext()).andStubReturn(context);
		replay(credentials);

		AbsolutifyingOidcClient partialMock = createMockBuilder(AbsolutifyingOidcClient.class)
				.withConstructor()
				.addMockedMethod("getUserAccessTokenResponse")
				.addMockedMethod("getUserInfo")
				.addMockedMethod("getClaimsSet")
				.createMock();

		expect(partialMock.getUserAccessTokenResponse(eq(credentials))).andStubReturn(accessTokenResponse);
		expect(partialMock.getUserInfo(anyObject(BearerAccessToken.class))).andStubReturn(userInfo);
		expect(partialMock.getClaimsSet(eq(idToken))).andStubReturn(claimsSet);
		replay(partialMock);

		partialMock.addCustomParam("useNonce", "true");
		initializeClient(partialMock, resourceRetriever);
		
		thrown.expect(TechnicalException.class);
		thrown.expectMessage(containsString("possible threat of cross-site request forgery"));

		partialMock.retrieveUserProfile(credentials, context);
	}
	
	private void initializeClient(AbsolutifyingOidcClient client, ResourceRetriever resourceRetriever) {
		client.setClientID("flooby1234");
		client.setSecret("dhfdsuyfbnjkdsz.ndifszbfzs");
		client.setDiscoveryURI("http://localhost/discovery");
		client.setCallbackUrl("http://example.com/");
		client.setResourceRetriever(resourceRetriever);
		client.internalInit();
	}
}
