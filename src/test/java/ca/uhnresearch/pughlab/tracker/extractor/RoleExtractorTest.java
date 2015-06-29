package ca.uhnresearch.pughlab.tracker.extractor;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.junit.Assert;

import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;

import ca.uhnresearch.pughlab.tracker.dao.AuthorizationRepository;
import ca.uhnresearch.pughlab.tracker.dto.Role;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class RoleExtractorTest extends AbstractShiroTest {

	private class TraceRestlet extends Restlet {
		// Does snothing, but prevents warning shouts
	}

	private RoleExtractor extractor;
	private AuthorizationRepository repository;

	@Before
	public void initialize() {
		
		Restlet mock = new TraceRestlet();
		extractor = new RoleExtractor();
		extractor.setRepository(repository);
		extractor.setNext(mock);
	}

	@Test
	public void testBasicExtraction() {
		
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
		extractor.handle(request, response);
		
		Role foundRole = (Role) request.getAttributes().get("role");
		Assert.assertNotNull(foundRole);
		Assert.assertEquals( "ROLE_CAT_HERDER", foundRole.getName() );
	}
}
