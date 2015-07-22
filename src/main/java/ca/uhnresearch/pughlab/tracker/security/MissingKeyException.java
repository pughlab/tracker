package ca.uhnresearch.pughlab.tracker.security;

import com.nimbusds.jose.Algorithm;

/**
 * Used to signal that we failed to verify a JWT due to a missing key. 
 * This should provoke a request to retrieve the updated keys. Only after
 * this has happened and a second attempt to verify failed should this count
 * as a truly bad signature. 
 * @author stuartw
 *
 */
public class MissingKeyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7421798547362548773L;

	public MissingKeyException(Algorithm alg, String kid) {
		super();
		this.alg = alg;
		this.kid = kid;
	}

	private Algorithm alg;
	
	private String kid;

	/**
	 * @return the alg
	 */
	public Algorithm getAlg() {
		return alg;
	}

	/**
	 * @return the kid
	 */
	public String getKid() {
		return kid;
	}
}
