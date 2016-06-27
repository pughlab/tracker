package ca.uhnresearch.pughlab.tracker.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MergingTokenizer implements Tokenizer {
	
	private Tokenizer input;
	
	private List<Token> tokens = new ArrayList<Token>();
	
	public MergingTokenizer(Tokenizer i) throws IOException, InvalidTokenException {
		super();
		input = i;
		
		// First collect all the tokens
		while(true) {
			final Token token = input.getNextToken();
			if (token == null) break;
			tokens.add(token);
		}
		
		// Now merge the tokens, repeatedly applying VALUE WS VALUE -> VALUE
		int index = 0;
		while (index++ < tokens.size() - 2) {
			final Token t1 = tokens.get(index - 1);
			if (! (t1 instanceof ValueToken)) continue;
			final Token t2 = tokens.get(index + 0);
			if (! (t2 instanceof WhitespaceToken)) continue;
			final Token t3 = tokens.get(index + 1);
			if (! (t3 instanceof ValueToken)) continue;
			
			t1.setValue(t1.getValue() + t2.getValue() + t3.getValue());
			index--;
			tokens.remove(index + 2);
			tokens.remove(index + 1);
			continue;
		}
		
		// And finally, remove the whitespace
		index = 0;
		while (index < tokens.size()) {
			final Token t1 = tokens.get(index);
			if (t1 instanceof WhitespaceToken) {
				tokens.remove(index);
			} else {
				index++;
			}
		}
	}

	@Override
	public Token getNextToken() throws IOException, InvalidTokenException {
		if (tokens.isEmpty()) {
			return null;
		} else {
			return tokens.remove(0);
		}
	}

}
