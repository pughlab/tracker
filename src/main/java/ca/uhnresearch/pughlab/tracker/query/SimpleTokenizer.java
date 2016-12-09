package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;
import java.io.Reader;

/**
 * Converts a string into a set of tokens. This is a somewhat quick-and-dirty
 * tokenizer that is mostly UNICODE sound and generates the fairly small set
 * of tokens that we are interested in. 
 * 
 * @author stuartw
 *
 */
public class SimpleTokenizer implements Tokenizer {
	
	/**
	 * Behind the simple tokenizer, there is a {@link Reader} for the
	 * input.
	 */
	private final Reader input;
	
	/**
	 * If the peek is empty, it'll contain this.
	 */
	private static final int PEEK_EMPTY = -1;
	
	/**
	 * If we peeked, we'll store what we peeked at here.
	 */
	private int peek = PEEK_EMPTY;
	
	/**
	 * To assemble a token, we use an inner {@link StringBuilder}.
	 */
	private StringBuilder tokenBuilder = new StringBuilder();
	
	/**
	 * Returns the next character, managing peeking logic if we have 
	 * used it.
	 * 
	 * @return the next character
	 * @throws IOException
	 */
	private int getNextCharacter() throws IOException {
		if (peek != PEEK_EMPTY) {
			final int result = peek;
			peek = PEEK_EMPTY;
			return result;
		} else {
			return input.read();
		}
	}
	
	/**
	 * Marks a character as unread.
	 * @param character
	 */
	private void ungetCharacter(int character) {
		if (character != PEEK_EMPTY) {
			peek = character;
		}
	}
	
	/**
	 * Basic constructor, which takes a reader and wraps it in a tokenizer.
	 * @param input the reader
	 */
	public SimpleTokenizer(Reader input) {
		super();
		this.input = input;
	}

	/**
	 * Returns the next token from the tokenizer.
	 * @return the next token
	 */
	public Token getNextToken() throws IOException, InvalidTokenException {
		tokenBuilder.setLength(0);
		
		while(true) {
			final int tokenStart = getNextCharacter();
			final char tokenChar = (char) tokenStart;
			
			if (tokenStart == PEEK_EMPTY) {
				
				// At the end, return null to signal there's no token
				return null;
			
			} else if (tokenChar == '"') {
				
				// Handles a quoted string.
				tokenBuilder.append(tokenChar);
				while(true) {
					final int constituent = getNextCharacter();
					final char constituentChar = (char) constituent;
					if (constituent == PEEK_EMPTY) {
						throw new InvalidTokenException("Missing end quote");
					} else if (constituentChar != '"') {
						tokenBuilder.append(constituentChar);
					} else {
						tokenBuilder.append(constituentChar);
						return new QuotedStringToken(tokenBuilder.toString());
					}
				}

			} else if (Character.isWhitespace(tokenChar)) {
				// Do nothing. We can skip to the next character and attempt to
				// tokenize again
				
				tokenBuilder.append(tokenChar);
				while(true) {
					final int constituent = getNextCharacter();
					final char constituentChar = (char) constituent;
					if (constituent != PEEK_EMPTY && Character.isWhitespace(constituentChar)) {
						tokenBuilder.append(constituentChar);
					} else {
						ungetCharacter(constituent);
						
						final String token = tokenBuilder.toString();
						return new WhitespaceToken(token);
					}
				}
				
			} else if (tokenChar == '(' || tokenChar == ')' || tokenChar == ',') {
				
				// Character operators
				return new OperatorToken(Character.toString(tokenChar));
				
			} else {
				
				// Anything else is not whitespace. Add elements that aren't
				// whitespace or a terminator into a new token.
				
				tokenBuilder.append(tokenChar);
				while(true) {
					final int constituent = getNextCharacter();
					final char constituentChar = (char) constituent;
					if (constituent == PEEK_EMPTY || constituentChar == '(' || constituentChar == ')' || constituentChar == ',' || Character.isWhitespace(constituentChar)) {
						
						// At the end, so put back the thing we just found
						ungetCharacter(constituent);
						
						// Now what we have might be an operator
						final String token = tokenBuilder.toString();
						if (OperatorToken.isOperator(token)) {
							return new OperatorToken(token);
						} else {
							return new ValueToken(token);
						}
					
					} else {
					
						tokenBuilder.append(constituentChar);
					}
				}
			}
		}
	}
}
