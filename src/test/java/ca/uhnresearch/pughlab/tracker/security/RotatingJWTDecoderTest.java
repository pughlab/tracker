package ca.uhnresearch.pughlab.tracker.security;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class RotatingJWTDecoderTest {
	
	private RotatingJWTDecoder jwtDecoder;
	
	@Before
	public void setUp() {
		jwtDecoder = new RotatingJWTDecoder();
	}

	/**
	 * Test method for {@link ca.uhnresearch.pughlab.tracker.security.RotatingJWTDecoder#RotatingJWTDecoder()}.
	 */
	@Test
	public final void testDefaultJWTDecoderConstructor() {
		assertTrue(jwtDecoder instanceof RotatingJWTDecoder);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Test method for {@link ca.uhnresearch.pughlab.tracker.security.RotatingJWTDecoder#getJWSVerifier(SignedJWT)}.
	 */
	@Test
	public final void testGetJWSVerifier() {
		
		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		ReadOnlyJWTClaimsSet claims = new JWTClaimsSet();
		SignedJWT signedJwt = new SignedJWT(header, claims);
		
		thrown.expect(MissingKeyException.class);

		jwtDecoder.getJWSVerifier(signedJwt);
	}
	
	/**
	 * Test method for {@link ca.uhnresearch.pughlab.tracker.security.RotatingJWTDecoder#addJWSVerifier(SignedJWT)}.
	 */
	@Test
	public final void testAddJWSVerifier() {
		
		Set<JWSAlgorithm> algorithms = new HashSet<JWSAlgorithm>();
		algorithms.add(JWSAlgorithm.RS256);
		
		JWSVerifier verifier = createMock(JWSVerifier.class);
		expect(verifier.getAcceptedAlgorithms()).andStubReturn(algorithms);
		replay(verifier);

		jwtDecoder.addJWSVerifier(verifier, null);
		
		// And now check we can retrieve it
		
		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		ReadOnlyJWTClaimsSet claims = new JWTClaimsSet();
		SignedJWT signedJwt = new SignedJWT(header, claims);

		
		JWSVerifier found = jwtDecoder.getJWSVerifier(signedJwt);
		Assert.assertNotNull(found);
	}

	/**
	 * Test method for {@link ca.uhnresearch.pughlab.tracker.security.RotatingJWTDecoder#addJWSVerifier(SignedJWT)}.
	 */
	@Test
	public final void testAddJWSVerifierWithKeyId() {
		
		Set<JWSAlgorithm> algorithms = new HashSet<JWSAlgorithm>();
		algorithms.add(JWSAlgorithm.RS256);
		
		JWSVerifier verifier = createMock(JWSVerifier.class);
		expect(verifier.getAcceptedAlgorithms()).andStubReturn(algorithms);
		replay(verifier);

		jwtDecoder.addJWSVerifier(verifier, "flooby");
		
		// And now check we can't retrieve it
		
		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		ReadOnlyJWTClaimsSet claims = new JWTClaimsSet();
		SignedJWT signedJwt = new SignedJWT(header, claims);

		thrown.expect(MissingKeyException.class);
		jwtDecoder.getJWSVerifier(signedJwt);
		
		// With the right key identifier, however, we should be able to find it
		
		header.setKeyID("flooby");
		signedJwt = new SignedJWT(header, claims);
		
		JWSVerifier found = jwtDecoder.getJWSVerifier(signedJwt);
		Assert.assertNotNull(found);
		Assert.assertEquals(verifier, found);
	}
	
	/**
	 * Test method for {@link ca.uhnresearch.pughlab.tracker.security.RotatingJWTDecoder#addJWSVerifier(SignedJWT)}.
	 */
	@Test
	public final void testAddJWEDecrypter() {
		
		Set<JWEAlgorithm> algorithms = new HashSet<JWEAlgorithm>();
		algorithms.add(JWEAlgorithm.RSA1_5);
		
		JWEDecrypter decrypter = createMock(JWEDecrypter.class);
		expect(decrypter.getAcceptedAlgorithms()).andStubReturn(algorithms);
		replay(decrypter);

		jwtDecoder.addJWEDecrypter(decrypter, null);
	}


	/**
	 * Test method for {@link ca.uhnresearch.pughlab.tracker.security.RotatingJWTDecoder#decodeJWT(JWT)}.
	 * @throws ParseException 
	 * @throws JOSEException 
	 */
	@Test
	public final void testDecodeSignedJWT() throws JOSEException, ParseException {
		
		Set<JWSAlgorithm> algorithms = new HashSet<JWSAlgorithm>();
		algorithms.add(JWSAlgorithm.RS256);
		
		JWSVerifier verifier = createMock(JWSVerifier.class);
		expect(verifier.getAcceptedAlgorithms()).andStubReturn(algorithms);
		replay(verifier);

		jwtDecoder.addJWSVerifier(verifier, null);
		
		JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
		ReadOnlyJWTClaimsSet claims = new JWTClaimsSet();
		SignedJWT signedJwt = new SignedJWT(header, claims);

		thrown.expect(JOSEException.class);

		jwtDecoder.decodeJWT(signedJwt);
	}

	/**
	 * Test method for {@link ca.uhnresearch.pughlab.tracker.security.RotatingJWTDecoder#decodeJWT(JWT)}.
	 * @throws ParseException 
	 * @throws JOSEException 
	 */
	@Test
	public final void testDecodePlainJWT() throws JOSEException, ParseException {
		
		PlainHeader header = new PlainHeader();
		ReadOnlyJWTClaimsSet claims = new JWTClaimsSet();
		PlainJWT plainJwt = new PlainJWT(header, claims);

		ReadOnlyJWTClaimsSet result = jwtDecoder.decodeJWT(plainJwt);
		Assert.assertNotNull(result);
	}

}
