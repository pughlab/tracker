package ca.uhnresearch.pughlab.tracker.domain;

import java.sql.Types;

import ca.uhnresearch.pughlab.tracker.dto.CaseAttributeBase;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public abstract class QCaseAttributeBase<T extends CaseAttributeBase> extends com.mysema.query.sql.RelationalPathBase<T> {
	
	private static final long serialVersionUID = 1L;

	public final StringPath attribute = createString("attribute");

    public final NumberPath<Integer> caseId = createNumber("caseId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath notAvailable = createBoolean("notAvailable");

    public final StringPath notes = createString("notes");

	public QCaseAttributeBase(Class<? extends T> type,
			PathMetadata<?> metadata, String schema, String table) {
		super(type, metadata, schema, table);
	}

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(caseId, ColumnMetadata.named("CASE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(attribute, ColumnMetadata.named("ATTRIBUTE").withIndex(3).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(notAvailable, ColumnMetadata.named("NOT_AVAILABLE").withIndex(5).ofType(Types.BIT).notNull());
        addMetadata(notes, ColumnMetadata.named("NOTES").withIndex(6).ofType(Types.VARCHAR).withSize(2048));
    }
}
