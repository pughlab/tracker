package ca.uhnresearch.pughlab.tracker.security;

import static org.easymock.EasyMock.*;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;

import java.util.ArrayList;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.test.AbstractShiroTest;

public class AuditedSecurityManagerTest extends AbstractShiroTest {

	private DefaultWebSecurityManager manager;

	@Before
	public void initialize() {
		DefaultWebSessionManager sessions = new DefaultWebSessionManager();
		
		manager = new DefaultWebSecurityManager();
		manager.setSessionManager(sessions);
		setSecurityManager(manager);
	}
	
	@After
	public void tearDown() {
		manager = null;
		setSecurityManager(null);
		clearSubject();
	}

	@Test
	public void testGetSetRepository() {
		
		AuditedSecurityManager manager = new AuditedSecurityManager();
		
		AuditLogRepository repository = createMock(AuditLogRepository.class);
		replay(repository);
		
		manager.setAuditLogRepository(repository);
		
		Assert.assertEquals(repository, manager.getAuditLogRepository());
	}

	@Test
	public void testLogin() {
		
		AuditedSecurityManager manager = new AuditedSecurityManager();
				
		Capture<AuditLogRecord> capturedArgument = EasyMock.newCapture(CaptureType.FIRST);

		AuditLogRepository repository = createMock(AuditLogRepository.class);
		repository.writeAuditLogRecord(capture(capturedArgument));
		expectLastCall();
		replay(repository);
		
		manager.setAuditLogRepository(repository);
		
		PrincipalCollection principals = new SimplePrincipalCollection("stuart", "mock");
		
		AuthenticationInfo info = createMock(AuthenticationInfo.class);
		expect(info.getPrincipals()).andStubReturn(principals);
		replay(info);

		Realm realm = createMock(Realm.class);
		expect(realm.supports(anyObject(AuthenticationToken.class))).andStubReturn(true);
		expect(realm.getAuthenticationInfo(anyObject(AuthenticationToken.class))).andStubReturn(info);
		replay(realm);
		manager.setRealm(realm);

		AuthenticationToken token = createMock(AuthenticationToken.class);
		replay(token);
		
		Session session = createMock(Session.class);
		expect(session.getHost()).andStubReturn("localhost");
		expect(session.getId()).andStubReturn("123");
		expect(session.getAttribute("org.apache.shiro.subject.support.DelegatingSubject.RUN_AS_PRINCIPALS_SESSION_KEY")).andStubReturn(new ArrayList<String>());
		expect(session.getAttribute("org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY")).andStubReturn(principals);
		expect(session.getAttribute("org.apache.shiro.subject.support.DefaultSubjectContext_AUTHENTICATED_SESSION_KEY")).andStubReturn(false);
		session.setAttribute("org.apache.shiro.subject.support.DefaultSubjectContext_AUTHENTICATED_SESSION_KEY", true);
		expectLastCall();
		replay(session);

		Subject subject = createMock(Subject.class);
		expect(subject.getPrincipals()).andStubReturn(principals);
		expect(subject.hasRole("ROLE_ADMIN")).andStubReturn(false);
		expect(subject.getSession(anyBoolean())).andStubReturn(session);
		replay(subject);
		
		manager.login(subject, token);
		
		Assert.assertEquals("stuart", capturedArgument.getValue().getEventUser());

		verify(repository);
	}


	@Test
	public void testLogout() {
		AuditedSecurityManager manager = new AuditedSecurityManager();
		
		Capture<AuditLogRecord> capturedArgument = EasyMock.newCapture(CaptureType.FIRST);

		AuditLogRepository repository = createMock(AuditLogRepository.class);
		repository.writeAuditLogRecord(capture(capturedArgument));
		expectLastCall();
		replay(repository);
		
		manager.setAuditLogRepository(repository);

		PrincipalCollection principals = new SimplePrincipalCollection("stuart", "mock");

		Session session = createMock(Session.class);
		session.stop();
		expectLastCall().anyTimes();
		expect(session.removeAttribute("org.apache.shiro.subject.support.DefaultSubjectContext_AUTHENTICATED_SESSION_KEY")).andStubReturn(null);
		expect(session.removeAttribute("org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY")).andStubReturn(null);
		replay(session);

		Subject subject = createMock(Subject.class);
		expect(subject.getPrincipals()).andStubReturn(principals);
		expect(subject.hasRole("ROLE_ADMIN")).andStubReturn(false);
		expect(subject.getSession(anyBoolean())).andStubReturn(session);
		replay(subject);
		
		manager.logout(subject);
		
		Assert.assertEquals("stuart", capturedArgument.getValue().getEventUser());
		
		verify(repository);
	}
}
