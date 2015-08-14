package ca.uhnresearch.pughlab.tracker.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

public class LdapRealmTest {

	@Test
	public void testConstructor() {
		LdapRealm realm = new LdapRealm();
		Assert.assertNotNull(realm);
	}

	@Test
	public void testSetContexts() {
		
		DomainLdapContext context = createMock(DomainLdapContext.class);
		replay(context);
		
		List<LdapContext> contexts = new ArrayList<LdapContext>();
		contexts.add(context);
		
		LdapRealm realm = new LdapRealm();
		realm.setLdapContexts(contexts);
	}

}
