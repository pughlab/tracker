package ca.uhnresearch.pughlab.tracker.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Mechanism;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oidc.profile.OidcProfile;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Request;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCAccessTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.util.DefaultResourceRetriever;
import com.nimbusds.openid.connect.sdk.util.ResourceRetriever;

public class AbsolutifyingOidcClient extends BaseClient<ContextualOidcCredentials, OidcProfile> {
	
    /* Parameter to indicate to send nonce in the authentication request */
    private static final String USE_NONCE_PARAM = "useNonce";

    /* state attribute name in session */
    private static final String STATE_ATTRIBUTE = "oidcStateAttribute";

    /* nonce attribute name in session */
    private static final String NONCE_ATTRIBUTE = "oidcNonceAttribute";

    /* OpenID client_id */
    private String clientId;

    /* OpenID secret */
    private String secret;

    /* OpenID redirect_uri */
    private URI redirectURI;

    /* discovery URI for fetching OP metadata (http://openid.net/specs/openid-connect-discovery-1_0.html) */
    private String discoveryURI;

    /* Decoder for the JWT ID Token */
    private RotatingJWTDecoder jwtDecoder;

    /* OIDC metadata */
    private OIDCProviderMetadata oidcProvider;

    /* Map containing the parameters for configuring all aspects of the OpenID Connect integration */
    private Map<String, String> authParams;

    /* Map containing user defined parameters */
    private final Map<String, String> customParams = new HashMap<String, String>();

    /* client authentication object at the token End Point (basic, form or JWT) */
    private ClientAuthentication clientAuthentication;

    /* clientID object */
    private ClientID _clientID;

    /* secret object */
    private Secret _secret;
    
    /* resource retriever */
    private ResourceRetriever resourceRetriever = new DefaultResourceRetriever();

    @Override
    public Mechanism getMechanism() {
        return Mechanism.OPENID_CONNECT_PROTOCOL;
    }

    public void setDiscoveryURI(final String discoveryURI) {
        this.discoveryURI = discoveryURI;
    }

    public void setClientID(final String clientID) {
        this.clientId = clientID;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public void addCustomParam(final String key, final String value) {
        this.customParams.put(key, value);
    }
    
    public ResourceRetriever getResourceRetriever() {
    	return resourceRetriever;
    }
    
    public void setResourceRetriever(ResourceRetriever resourceRetriever) {
    	this.resourceRetriever = resourceRetriever;
    }

    @Override
    protected void internalInit() {

        CommonHelper.assertNotBlank(this.clientId, "clientID cannot be blank");
        CommonHelper.assertNotBlank(this.secret, "secret cannot be blank");
        CommonHelper.assertNotBlank(this.discoveryURI, "discoveryURI cannot be blank");

        this.authParams = new HashMap<String, String>();
        // default values
        this.authParams.put("scope", "openid email profile");
        this.authParams.put("response_type", "code");
        this.authParams.put("prompt", "login");
        this.authParams.put("display", "page");
        this.authParams.put("redirect_uri", getCallbackUrl());
        // add custom values
        this.authParams.putAll(this.customParams);
        // Override with required values
        this.authParams.put("client_id", this.clientId);
        this.authParams.put("client_secret", this.secret);

        this._clientID = new ClientID(this.clientId);
        this._secret = new Secret(this.secret);

        JWKSet jwkSet;
        // Download OIDC metadata and Json Web Key Set
        try {
            ResourceRetriever resourceRetriever = getResourceRetriever();
            this.oidcProvider = OIDCProviderMetadata.parse(resourceRetriever.retrieveResource(
                    new URL(this.discoveryURI)).getContent());
            jwkSet = JWKSet.parse(resourceRetriever.retrieveResource(this.oidcProvider.getJWKSetURI().toURL())
                    .getContent());

            this.redirectURI = new URI(getCallbackUrl());
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
        // Get available client authentication method
        ClientAuthenticationMethod method = getClientAuthenticationMethod();
        this.clientAuthentication = getClientAuthentication(method);
        // Init JWT decoder
        this.jwtDecoder = new RotatingJWTDecoder();
        initJwtDecoder(this.jwtDecoder, jwkSet);

    }

    @Override
    protected BaseClient<ContextualOidcCredentials, OidcProfile> newClient() {
    	AbsolutifyingOidcClient client = new AbsolutifyingOidcClient();
        client.setClientID(this.clientId);
        client.setSecret(this.secret);
        client.setDiscoveryURI(this.discoveryURI);
        client.setAuthParams(this.authParams);

        return client;
    }

    @Override
    protected boolean isDirectRedirection() {
        return true;
    }

    @Override
    protected RedirectAction retrieveRedirectAction(final WebContext context) {

        Map<String, String> params = new HashMap<String, String>(this.authParams);

        // Init state for CSRF mitigation
        State state = new State();
        params.put("state", state.getValue());
        context.setSessionAttribute(STATE_ATTRIBUTE, state);
        // Init nonce for replay attack mitigation
        if (useNonce()) {
            Nonce nonce = new Nonce();
            params.put("nonce", nonce.getValue());
            context.setSessionAttribute(NONCE_ATTRIBUTE, nonce.getValue());
        }
        
        // Fix the redirect_uri
    	URI redirect = getAbsoluteRedirectURI(context);
    	params.put("redirect_uri", redirect.toString());

        // Build authentication request query string
        String queryString;
        try {
            queryString = AuthenticationRequest.parse(params).toQueryString();
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
        String location = this.oidcProvider.getAuthorizationEndpointURI().toString() + "?" + queryString;
        logger.debug("Authentication request url : {}", location);

        return RedirectAction.redirect(location);
    }

    @Override
    protected ContextualOidcCredentials retrieveCredentials(final WebContext context) throws RequiresHttpAction {

        // Parse authentication response parameters
        Map<String, String> parameters = toSingleParameter(context.getRequestParameters());
        AuthenticationResponse response;
        try {
            response = AuthenticationResponseParser.parse(getAbsoluteRedirectURI(context), parameters);
        } catch (ParseException e) {
            throw new TechnicalException(e);
        }

        if (response instanceof AuthenticationErrorResponse) {
            logger.error("Bad authentication response, error={}",
                    ((AuthenticationErrorResponse) response).getErrorObject());
            return null;
        }

        logger.debug("Authentication response successful, get authorization code");
        AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) response;

        // state value must be equal
        logger.debug("Authentication response successful, get authorization code");
        logger.debug("Context state: {}", context.getSessionAttribute(STATE_ATTRIBUTE));
        logger.debug("Response state: {}", successResponse.getState());
        if (!successResponse.getState().equals(context.getSessionAttribute(STATE_ATTRIBUTE))) {
            throw new TechnicalException("State parameter is different from the one sent in authentication request. "
                    + "Session expired or possible threat of cross-site request forgery");
        }
        // Get authorization code
        AuthorizationCode code = successResponse.getAuthorizationCode();

        return new ContextualOidcCredentials(code, context);
    }
    
    protected HTTPResponse getHTTPResponse(Request request) throws SerializeException, IOException {
    	return request.toHTTPRequest().send();
    }
    
    protected OIDCAccessTokenResponse getUserAccessTokenResponse(final ContextualOidcCredentials credentials) throws Exception {
        TokenRequest request = new TokenRequest(this.oidcProvider.getTokenEndpointURI(), this.clientAuthentication,
                new AuthorizationCodeGrant(credentials.getCode(), getAbsoluteRedirectURI(credentials.getContext()),
                        this.clientAuthentication.getClientID()));
        HTTPResponse httpResponse = getHTTPResponse(request);
        
        logger.debug("Token response: status={}, content={}", httpResponse.getStatusCode(),
                httpResponse.getContent());

        TokenResponse response = OIDCTokenResponseParser.parse(httpResponse);
        if (response instanceof TokenErrorResponse) {
            logger.error("Bad token response, error={}", ((TokenErrorResponse) response).getErrorObject());
            return null;
        }
        logger.debug("Token response successful");
        OIDCAccessTokenResponse tokenSuccessResponse = (OIDCAccessTokenResponse) response;

        return tokenSuccessResponse;
    }
    
    protected UserInfo getUserInfo(final BearerAccessToken accessToken) throws Exception {
        UserInfo userInfo = null;
        if (this.oidcProvider.getUserInfoEndpointURI() != null) {
            UserInfoRequest userInfoRequest = new UserInfoRequest(this.oidcProvider.getUserInfoEndpointURI(), accessToken);
            HTTPResponse httpResponse = getHTTPResponse(userInfoRequest);
            logger.debug("Token response: status={}, content={}", httpResponse.getStatusCode(),
                    httpResponse.getContent());

            UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);

            if (userInfoResponse instanceof UserInfoErrorResponse) {
                logger.error("Bad User Info response, error={}",
                        ((UserInfoErrorResponse) userInfoResponse).getErrorObject());
            } else {
                UserInfoSuccessResponse userInfoSuccessResponse = (UserInfoSuccessResponse) userInfoResponse;
                userInfo = userInfoSuccessResponse.getUserInfo();
            }
        }
        
        return userInfo;
    }
    
    protected ReadOnlyJWTClaimsSet requestNewKeyAndVerify(final JWT idToken) throws JOSEException, java.text.ParseException {
        JWKSet jwkSet;
        // Download OIDC metadata and Json Web Key Set
        try {
            ResourceRetriever resourceRetriever = getResourceRetriever();
            this.oidcProvider = OIDCProviderMetadata.parse(resourceRetriever.retrieveResource(
                    new URL(this.discoveryURI)).getContent());
            jwkSet = JWKSet.parse(resourceRetriever.retrieveResource(this.oidcProvider.getJWKSetURI().toURL())
                    .getContent());
        } catch (Exception e2) {
            throw new TechnicalException(e2);
        }
        initJwtDecoder(this.jwtDecoder, jwkSet);

    	// Try to validate again -- a second failure here is going to be bad
    	return this.jwtDecoder.decodeJWT(idToken);
    }
    
    protected ReadOnlyJWTClaimsSet getClaimsSet(final JWT idToken) throws Exception {
        ReadOnlyJWTClaimsSet claimsSet;
        try {
        	claimsSet = this.jwtDecoder.decodeJWT(idToken);
        } catch (MissingKeyException e) {
        	
        	// Try to validate again -- a second failure here is going to be bad
        	claimsSet = requestNewKeyAndVerify(idToken);
        };

        return claimsSet;
    }

    @Override
    protected OidcProfile retrieveUserProfile(final ContextualOidcCredentials credentials, final WebContext context) {

        try {
            OIDCAccessTokenResponse tokenSuccessResponse = getUserAccessTokenResponse(credentials);
            BearerAccessToken accessToken = (BearerAccessToken) tokenSuccessResponse.getAccessToken();

            UserInfo userInfo = getUserInfo(accessToken);
            
            // Check ID Token
            ReadOnlyJWTClaimsSet claimsSet = getClaimsSet(tokenSuccessResponse.getIDToken());
            
            if (useNonce()) {
                String nonce = claimsSet.getStringClaim("nonce");
                if (nonce == null || !nonce.equals(credentials.getContext().getSessionAttribute(NONCE_ATTRIBUTE))) {
                    throw new TechnicalException(
                            "A nonce was sent in the authentication request but it is missing or different in the ID Token. "
                                    + "Session expired or possible threat of cross-site request forgery");
                }
            }

            // Return profile with Claims Set, User Info and Access Token
            OidcProfile profile = new OidcProfile(accessToken);
            profile.setId(claimsSet.getSubject());
            profile.addAttributes(claimsSet.getAllClaims());
            profile.addAttributes(userInfo.toJWTClaimsSet().getAllClaims());

            return profile;

        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }

    /**
     * Returns the first available authentication method from the OP.
     *
     * @return
     */
    private ClientAuthenticationMethod getClientAuthenticationMethod() {
        return this.oidcProvider.getTokenEndpointAuthMethods() != null
                && this.oidcProvider.getTokenEndpointAuthMethods().size() > 0 ? this.oidcProvider
                .getTokenEndpointAuthMethods().get(0) : ClientAuthenticationMethod.getDefault();
    }

    /**
     * Returns <code>true</code> if we want to use a nonce.
     *
     * @return
     */
    private boolean useNonce() {
        return Boolean.parseBoolean(this.authParams.get(USE_NONCE_PARAM));
    }

    /**
     * Add the required verifiers and decrypters to the JWT Decoder based on the JWK set from the OP.
     *
     * @param jwtDecoder
     * @param jwkSet
     */
    private void initJwtDecoder(final RotatingJWTDecoder jwtDecoder, final JWKSet jwkSet) {
        try {
            for (JWK key : jwkSet.getKeys()) {
                if (key.getKeyUse() == null || key.getKeyUse() == KeyUse.SIGNATURE) {
                    jwtDecoder.addJWSVerifier(getVerifier(key), key.getKeyID());
                }
            }
        } catch (Exception e) {
            throw new TechnicalException(e);
        }
    }

    private JWSVerifier getVerifier(final JWK key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (key instanceof RSAKey) {
            return new RSASSAVerifier(((RSAKey) key).toRSAPublicKey());
        } else if (key instanceof ECKey) {
            ECKey ecKey = (ECKey) key;
            return new ECDSAVerifier(ecKey.getX().decodeToBigInteger(), ecKey.getY().decodeToBigInteger());
        }
        return null;
    }

    /**
     * Returns a configured Client Authentication with method, client_id and secret for the token End Point.
     *
     * @param method
     * @return
     */
    private ClientAuthentication getClientAuthentication(final ClientAuthenticationMethod method) {
        if (ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(method)) {
            return new ClientSecretPost(this._clientID, this._secret);
        } else if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(method)) {
            return new ClientSecretBasic(this._clientID, this._secret);
        }
        return null;
    }

    private Map<String, String> toSingleParameter(final Map<String, String[]> requestParameters) {
        Map<String, String> map = new HashMap<String, String>();
        for (Entry<String, String[]> entry : requestParameters.entrySet()) {
            map.put(entry.getKey(), entry.getValue()[0]);
        }
        return map;
    }

    private void setAuthParams(final Map<String, String> authParams) {
        this.authParams = authParams;
    }

    protected URI getAbsoluteRedirectURI(WebContext context) {
    	URI result = this.redirectURI;
    	try {
			result = getAbsoluteUri(result, context);
		} catch (URISyntaxException e) {
			// Leave it alone
		}
    	return result;
    }
    
    protected URI getAbsoluteUri(URI location, WebContext context) throws URISyntaxException {
    	
		URIBuilder redirect = new URIBuilder(location);
		if (redirect.getScheme() == null) {
			redirect.setScheme(context.getScheme());
			redirect.setHost(context.getServerName());
			redirect.setPort(context.getServerPort());
		}
    	
    	return redirect.build();
    }
}
