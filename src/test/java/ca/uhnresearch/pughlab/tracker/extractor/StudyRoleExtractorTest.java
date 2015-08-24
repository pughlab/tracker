package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.matchers.JUnitMatchers.containsString;

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
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class StudyRoleExtractorTest extends AbstractShiroTest {

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
		extractor = new StudyRoleExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}
	
	@Test
	public void testNoStudy() throws RepositoryException {
        Subject subjectUnderTest = createMock(Subject.class);
        replay(subjectUnderTest);
        setSubject(subjectUnderTest);

		AuthorizationRepository mock = createMock(AuthorizationRepository.class);
		Role role = new Role();
		role.setName("ROLE_CAT_HERDER");
		role.setId(1234);
		expect(mock.getRole(anyObject(String.class))).andStubReturn(role);
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
		expect(study.getName()).andReturn("DEMO");
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

		request.getAttributes().put("study", study);
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

		request.getAttributes().put("study", study);
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
		role.setId(1234);
		expect(mock.getStudyRole(eq(study), anyObject(String.class))).andStubReturn(role);
		replay(mock);
		extractor.setRepository(mock);

		Reference reference = new Reference();
		Request request = new Request(Method.GET, reference);
		Response response = new Response(request);

		request.getAttributes().put("study", study);
		request.getAttributes().put("roleName", "ROLE_CAT_HERDER");
		
		extractor.handle(request, response);

		Role result = (Role) request.getAttributes().get("role");
		assertNotNull(role);
		
		Assert.assertEquals(role, result);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
	}
}
