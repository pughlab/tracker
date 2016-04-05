package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.*;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.oidc.profile.OidcProfile;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.User;
import ca.uhnresearch.pughlab.tracker.security.LdapProfile;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class UserExtractorTest extends AbstractShiroTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private UserExtractor extractor;
	private AuthorizationRepository repository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new UserExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}
	
	@Test
	public void testSaveUser() throws RepositoryException {
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		principals.add("stuart", "mock");

		Subject subjectUnderTest = createMock(Subject.class);
		expect(subjectUnderTest.getPrincipals()).andStubReturn(principals);
		expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Capture<User> capturedArgument = newCapture(CaptureType.FIRST);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		mock.saveUser(capture(capturedArgument));
		expectLastCall();
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		
		extractor.handle(request, response);
		
		Assert.assertEquals("stuart", capturedArgument.getValue().getUsername());
	}

	@Test
	public void testUpdateUserLDAP() throws RepositoryException {
		
		LdapProfile profile = new LdapProfile("stuart");
		profile.setDisplayName("Stuart Watt");
		profile.setFamilyName("Watt");
		profile.setGivenName("Stuart");
		
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		
		principals.add(profile, "mock");

		Subject subjectUnderTest = createMock(Subject.class);
		expect(subjectUnderTest.getPrincipals()).andStubReturn(principals);
		expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Capture<User> capturedArgument = newCapture(CaptureType.FIRST);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		mock.saveUser(capture(capturedArgument));
		expectLastCall();
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		
		extractor.handle(request, response);
		
		Assert.assertEquals("stuart", capturedArgument.getValue().getUsername());
		Assert.assertEquals("Stuart", capturedArgument.getValue().getGivenName());
		Assert.assertEquals("Watt", capturedArgument.getValue().getFamilyName());
	}

	@Test
	public void testUpdateUserOIDC() throws RepositoryException {
		
		OidcProfile profile = new OidcProfile();
		profile.addAttribute("preferred_username", "stuart");
		profile.addAttribute("name", "Stuart Watt");
		profile.addAttribute("family_name", "Watt");
		profile.addAttribute("given_name", "Stuart");
		profile.addAttribute("email", "stuart@example.com");
		
		SimplePrincipalCollection principals = new SimplePrincipalCollection();
		
		principals.add(profile, "mock");

		Subject subjectUnderTest = createMock(Subject.class);
		expect(subjectUnderTest.getPrincipals()).andStubReturn(principals);
		expect(subjectUnderTest.hasRole("ROLE_ADMIN")).andReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Capture<User> capturedArgument = newCapture(CaptureType.FIRST);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		mock.saveUser(capture(capturedArgument));
		expectLastCall();
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		
		extractor.handle(request, response);
		
		Assert.assertEquals("stuart", capturedArgument.getValue().getUsername());
		Assert.assertEquals("Stuart", capturedArgument.getValue().getGivenName());
		Assert.assertEquals("Watt", capturedArgument.getValue().getFamilyName());
		Assert.assertEquals("stuart@example.com", capturedArgument.getValue().getEmail());
	}
}
