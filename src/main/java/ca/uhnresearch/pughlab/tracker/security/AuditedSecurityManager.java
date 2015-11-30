package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;

public class AuditedSecurityManager extends DefaultWebSecurityManager {
	
	/**
	 * Private field for the audit log repository.
	 */
	private AuditLogRepository auditLogRepository;
	
	/**
	 * Handles login by additionally writing an audit record.
	 * @param subject the subject
	 * @param token the authentication token
	 * @return the logged in subject
	 */
	@Override
	public Subject login(Subject subject, AuthenticationToken token) {
		Subject result = super.login(subject, token);
		
		AuditLogRecord record = new AuditLogRecord();
		record.setEventType("login");
		record.setEventUser(result.getPrincipals().getPrimaryPrincipal().toString());
		auditLogRepository.writeAuditLogRecord(record);
		
		return result;
	}
	
	/**
	 * Handles logout by additionally writing an audit record.
	 * @param subject the subject
	 */
	@Override 
	public void logout(Subject subject) {
		PrincipalCollection principals = subject.getPrincipals();
		if (principals != null && principals.getPrimaryPrincipal() != null) {
			String username = subject.getPrincipals().getPrimaryPrincipal().toString();
			AuditLogRecord record = new AuditLogRecord();
			record.setEventType("logout");
			record.setEventUser(username);
			auditLogRepository.writeAuditLogRecord(record);
		}
		super.logout(subject);		
	}

	/**
	 * Reads the audit log repository.
	 * @return the auditLogRepository
	 */
	public AuditLogRepository getAuditLogRepository() {
		return auditLogRepository;
	}

	/**
	 * Sets the audit log repository.
	 * @param auditLogRepository the auditLogRepository to set
	 */
	public void setAuditLogRepository(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}
}
