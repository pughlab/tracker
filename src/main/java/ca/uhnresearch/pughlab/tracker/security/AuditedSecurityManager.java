package ca.uhnresearch.pughlab.tracker.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;

public class AuditedSecurityManager extends DefaultWebSecurityManager {
	
	private AuditLogRepository auditLogRepository;
	
	@Override
	public Subject login(Subject subject, AuthenticationToken token) {
		Subject result = super.login(subject, token);
		
		AuditLogRecord record = new AuditLogRecord();
		record.setEventType("login");
		record.setEventUser(result.getPrincipals().getPrimaryPrincipal().toString());
		auditLogRepository.writeAuditLogRecord(record);
		
		return result;
	}
	
	@Override 
	public void logout(Subject subject) {
		super.logout(subject);		
		
		AuditLogRecord record = new AuditLogRecord();
		record.setEventType("logout");
		record.setEventUser(subject.getPrincipals().getPrimaryPrincipal().toString());
		auditLogRepository.writeAuditLogRecord(record);
	}

	/**
	 * @return the auditLogRepository
	 */
	public AuditLogRepository getAuditLogRepository() {
		return auditLogRepository;
	}

	/**
	 * @param auditLogRepository the auditLogRepository to set
	 */
	public void setAuditLogRepository(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}
}
