package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.Matchers.containsString;

import org.apache.shiro.subject.Subject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dao.RepositoryException;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.dto.Study;
import ca.uhnresearch.pughlab.tracker.resource.RequestAttributes;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class RoleExtractorTest extends AbstractShiroTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private RoleExtractor extractor;
	private AuthorizationRepository repository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new RoleExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}
	
	@Test
	public void testNoStudy() throws RepositoryException {
        Subject subjectUnderTest = createMock(Subject.class);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		expect(study.getName()).andReturn("DEMO");
		replay(study);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		expect(mock.getStudyRole(eq(study), anyObject(String.class))).andStubReturn(role);
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);
		request.getAttributes().put("roleName", "ROLE_CAT_HERDER");
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Bad Request"));

		extractor.handle(request, response);
	}

	@Test
	public void testNoPermissions() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		expect(study.getName()).andStubReturn("DEMO");
		replay(study);

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(false);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);

		RequestAttributes.setRequestStudy(request, study);
		request.getAttributes().put("roleName", "ROLE_CAT_HERDER");
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Forbidden"));

		extractor.handle(request, response);
	}

	@Test
	public void testNotFound() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		expect(study.getName()).andReturn("DEMO");
		replay(study);

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		expect(mock.getStudyRole(eq(study), anyObject(String.class))).andStubReturn(null);
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);

		RequestAttributes.setRequestStudy(request, study);
		request.getAttributes().put("roleName", "ROLE_CAT_HERDER");
		
		thrown.expect(ResourceException.class);
		thrown.expectMessage(containsString("Not Found"));

		extractor.handle(request, response);
	}

	@Test
	public void testFound() throws RepositoryException {
		
		Study study = createMock(Study.class);
		expect(study.getId()).andReturn(1);
		expect(study.getName()).andReturn("DEMO");
		replay(study);

        Subject subjectUnderTest = createMock(Subject.class);
        expect(subjectUnderTest.isPermitted("DEMO:admin")).andStubReturn(true);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setStudyName("DEMO");
		role.setStudyId(321);
		role.setId(1234);
		expect(mock.getStudyRole(eq(study), anyObject(String.class))).andStubReturn(role);
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);

		RequestAttributes.setRequestStudy(request, study);
		request.getAttributes().put("roleName", "ROLE_CAT_HERDER");
		
		extractor.handle(request, response);

		Role result = RequestAttributes.getRequestRole(request);
		assertNotNull(role);
		
		Assert.assertEquals(role, result);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
}
