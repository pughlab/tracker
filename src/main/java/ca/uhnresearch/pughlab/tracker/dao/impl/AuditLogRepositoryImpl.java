package ca.uhnresearch.pughlab.tracker.dao.impl;

import static ca.uhnresearch.pughlab.tracker.domain.QAuditLog.auditLog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jdbc.query.SqlInsertCallback;

import ca.uhnresearch.pughlab.tracker.dao.AuditLogRepository;
import ca.uhnresearch.pughlab.tracker.dao.CasePager;
import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;
import ca.uhnresearch.pughlab.tracker.dto.Study;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLInsertClause;

public class AuditLogRepositoryImpl implements AuditLogRepository {

	private final Logger logger = LoggerFactory.getLogger(AuditLogRepositoryImpl.class);

	private QueryDslJdbcTemplate template;

	private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	private static ObjectMapper objectMapper = new ObjectMapper();

	@Required
    public void setTemplate(QueryDslJdbcTemplate template) {
        this.template = template;
    }

	/**
	 * Writes a new audit log record to the repository.
	 * @param record
	 */
	@Override
	public void writeAuditLogRecord(final AuditLogRecord record) {
    	template.insert(auditLog, new SqlInsertCallback() {
    		public long doInSqlInsertClause(SQLInsertClause sqlInsertClause) {
    			return sqlInsertClause.columns(auditLog.studyId, auditLog.caseId, auditLog.attribute, auditLog.eventType, auditLog.eventUser, auditLog.eventTime, auditLog.eventArgs)
    				.values(record.getStudyId(), record.getCaseId(), record.getAttribute(), record.getEventType(), record.getEventUser(), new Timestamp((new java.util.Date()).getTime()), record.getEventArgs())
    				.execute();
    		};
    	});
	}
	
	@Override
	public List<JsonNode> getAuditData(Study study, CasePager query) {
		
		SQLQuery sq = template.newSqlQuery().from(auditLog).where(auditLog.studyId.eq(study.getId())).orderBy(auditLog.id.desc());
		
		if (query.getOffset() != null) {
			sq = sq.offset(query.getOffset());
		}
		if (query.getLimit() != null) {
			sq = sq.limit(query.getLimit());
		}
	
    	final List<AuditLogRecord> data = template.query(sq, auditLog);
    	final List<JsonNode> result = new ArrayList<JsonNode>();
    	
    	for(AuditLogRecord audit : data) {
    		final ObjectNode obj = jsonNodeFactory.objectNode();
    		obj.put("caseId", audit.getCaseId());
    		obj.put("attribute", audit.getAttribute());
    		obj.put("eventTime", audit.getEventTime().toString());
    		obj.put("eventType", audit.getEventType());
    		obj.put("eventUser", audit.getEventUser());
			JsonNode argsNode = null;
			logger.debug("Got data: {}", audit.getEventArgs().toString());
			try {
				argsNode = objectMapper.readTree(audit.getEventArgs());
			} catch (Exception e) {
				logger.error("Invalid JSON arguments: {}, {}", e.getMessage(), audit.getEventArgs());
			}
			
			obj.replace("eventArgs", argsNode);
    		result.add(obj);
    	}

		return result;
	}


}
