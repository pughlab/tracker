package ca.uhnresearch.pughlab.tracker.security;

import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;

import net.jcip.annotations.ThreadSafe;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.util.JWTDecoder;

@ThreadSafe
public class RotatingJWTDecoder implements JWTDecoder {
	
	private class KeySelector implements Comparable<KeySelector> {
		
		private Algorithm algorithm;
		
		private String kid;
		
		KeySelector(Algorithm algorithm, String kid) {
			this.algorithm = algorithm;
			this.kid = kid;
		}

		@Override
		public int compareTo(KeySelector o2) {
			int alg = algorithm.getName().compareTo(o2.algorithm.getName());
			if (alg != 0) {
				return alg;
			} else {
				String kid1 = kid;
				String kid2 = o2.kid;
				return (kid1 == null && kid2 == null) ? 0 : kid1.compareTo(kid2);
			}
		}

		/**
		 * @return the algorithm
		 */
		public Algorithm getAlgorithm() {
			return algorithm;
		}

		/**
		 * @return the kid
		 */
		public String getKid() {
			return kid;
		}
	}
	
	/**
	 * Thread-safe map of available JWS verifiers.
	 */
	private final Map<KeySelector,JWSVerifier> jwsVerifiers = 
		new TreeMap<KeySelector,JWSVerifier>();
	
	
	/**
	 * Thread-safe map of available JWE decrypters.
	 */
	private final Map<KeySelector,JWEDecrypter> jweDecrypters = 
		new TreeMap<KeySelector,JWEDecrypter>();
	
	
	/**
	 * Creates a new decoder of JSON Web Tokens (JWTs). The decoder must 
	 * then be supplied with one or more configured JWS verifiers and / or 
	 * JWE decrypters.
	 */
	public RotatingJWTDecoder() {
		// Nothing to do
	}

	/**
	 * Adds the specified JWS verifier for decoding signed JWTs. The JWS 
	 * algorithms accepted by the verifier should match the ones used to 
	 * secure the expected JWTs.
	 *
	 * @param verifier The JWS verifier to add. Must be ready to verify
	 *                 signed JWTs and not {@code null}.
	 */
	public void addJWSVerifier(final JWSVerifier verifier, final String kid) {
	
		for (JWSAlgorithm alg: verifier.getAcceptedAlgorithms()) {
			KeySelector selector = new KeySelector(alg, kid);
			jwsVerifiers.put(selector, verifier);
		}
	}

	/**
	 * Adds the specified JWE decrypter for decoding encrypted JWTs. The
	 * JWE algorithms accepted by the decrypter should match the ones
	 * used to secure the expected JWTs.
	 *
	 * @param decrypter The JWE decrypter to add. Must be ready to decrypt
	 *                  encrypted JWTs and not {@code null}.
	 */
	public void addJWEDecrypter(final JWEDecrypter decrypter, final String kid) {
	
		for (JWEAlgorithm alg: decrypter.getAcceptedAlgorithms()) {
			KeySelector selector = new KeySelector(alg, kid);
			jweDecrypters.put(selector, decrypter);
		}
	}
	
	
	private KeySelector getKeySelector(final SignedJWT signedJWT) {
		JWSAlgorithm alg = signedJWT.getHeader().getAlgorithm();
		String kid = signedJWT.getHeader().getKeyID();
		return new KeySelector(alg, kid);
	}
	
	/**
	 * Verifies a signed JWT by calling the matching verifier for its JWS
	 * algorithm.
	 *
	 * @param signedJWT The signed JWT to verify. Must not be {@code null}.
	 *
	 * @return The JWT claims set.
	 *
	 * @throws JOSEException  If no matching JWS verifier was found, the 
	 *                        signature is bad or verification failed.
	 * @throws ParseException If parsing of the JWT claims set failed.
	 */
	private ReadOnlyJWTClaimsSet verify(final SignedJWT signedJWT)
		throws JOSEException, ParseException {
		
		KeySelector selector = getKeySelector(signedJWT);
		
		JWSVerifier verifier = jwsVerifiers.get(selector);
		
		if (verifier == null) {
			
			// If we can'ty find a verifier, that probably means that we need to check for new
			// keys and try again. 
			throw new MissingKeyException(selector.getAlgorithm(), selector.getKid());
		}
			
		
		boolean verified;

		try {
			verified = signedJWT.verify(verifier);

		} catch (IllegalStateException e) {

			throw new JOSEException(e.getMessage(), e);
		}
		
		if (! verified) {

			throw new JOSEException("Bad JWS signature");
		}
		
		return signedJWT.getJWTClaimsSet();
	}
	
	
	/**
	 * Decrypts an encrypted JWT by calling the matching decrypter for its
	 * JWE algorithm and encryption method.
	 *
	 * @param encryptedJWT The encrypted JWT to decrypt. Must not be 
	 *                     {@code null}.
	 *
	 * @return The JWT claims set.
	 *
	 * @throws JOSEException  If no matching JWE decrypter was found or if
	 *                        decryption failed.
	 * @throws ParseException If parsing of the JWT claims set failed.
	 */
	private ReadOnlyJWTClaimsSet decrypt(final EncryptedJWT encryptedJWT)
		throws JOSEException, ParseException {
		
		JWEAlgorithm alg = encryptedJWT.getHeader().getAlgorithm();
		
		JWEDecrypter decrypter = jweDecrypters.get(alg);
		
		if (decrypter == null) {

			throw new JOSEException("Unsupported JWE algorithm: " + alg);
		}
		
		
		try {
			encryptedJWT.decrypt(decrypter);

		} catch (IllegalStateException e) {

			throw new JOSEException(e.getMessage(), e);
		}
		
		return encryptedJWT.getJWTClaimsSet();
	}


	@Override
	public ReadOnlyJWTClaimsSet decodeJWT(final JWT jwt)
		throws JOSEException, ParseException {
		
		if (jwt instanceof PlainJWT) {
		
			PlainJWT plainJWT = (PlainJWT)jwt;
			
			return plainJWT.getJWTClaimsSet();
		
		} else if (jwt instanceof SignedJWT) {
		
			SignedJWT signedJWT = (SignedJWT)jwt;
			
			return verify(signedJWT);

		} else if (jwt instanceof EncryptedJWT) {
		
			EncryptedJWT encryptedJWT = (EncryptedJWT)jwt;
			
			return decrypt(encryptedJWT);
			
		} else {
		
			throw new JOSEException("Unexpected JWT type: " + jwt.getClass());
		}
	}

}
