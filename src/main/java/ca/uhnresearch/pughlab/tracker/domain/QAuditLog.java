package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Timestamp;
import java.sql.Types;

import ca.uhnresearch.pughlab.tracker.dto.AuditLogRecord;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.sql.RelationalPathBase;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public class QAuditLog extends RelationalPathBase<AuditLogRecord> {

	private static final long serialVersionUID = -3383936919070183550L;

    public static final QAuditLog auditLog = new QAuditLog("audit_log");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final NumberPath<Integer> caseId = createNumber("caseId", Integer.class);

    public final StringPath attribute = createString("attribute");

    public final DateTimePath<Timestamp> eventTime = createDateTime("eventTime", Timestamp.class);

    public final StringPath eventUser = createString("eventUser");

    public final StringPath eventType = createString("eventType");

    public final StringPath eventArgs = createString("eventArgs");

    public QAuditLog(String variable) {
        super(AuditLogRecord.class, forVariable(variable), "null", "AUDIT_LOG");
        addMetadata();
    }
    
    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(studyId, ColumnMetadata.named("STUDY_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(caseId, ColumnMetadata.named("CASE_ID").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(attribute, ColumnMetadata.named("ATTRIBUTE").withIndex(4).ofType(Types.VARCHAR).withSize(48).notNull());
        addMetadata(eventTime, ColumnMetadata.named("EVENT_TIME").withIndex(5).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(eventUser, ColumnMetadata.named("EVENT_USER").withIndex(6).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(eventType, ColumnMetadata.named("EVENT_TYPE").withIndex(7).ofType(Types.VARCHAR).withSize(12).notNull());
        addMetadata(eventArgs, ColumnMetadata.named("EVENT_ARGS").withIndex(8).ofType(Types.VARCHAR).withSize(2048));
    }
}
