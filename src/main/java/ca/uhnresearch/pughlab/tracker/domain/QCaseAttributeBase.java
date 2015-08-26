package ca.uhnresearch.pughlab.tracker.domain;

import java.sql.Types;

import ca.uhnresearch.pughlab.tracker.dto.CaseAttribute;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.expr.ComparableExpressionBase;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public abstract class QCaseAttributeBase<T extends Object> extends com.mysema.query.sql.RelationalPathBase<CaseAttribute> {
	
	private static final long serialVersionUID = 1L;

	public final NumberPath<Integer> attributeId = createNumber("attributeId", Integer.class);
	
	public static QCaseAttributeBase<?> caseAttributes;

    public final NumberPath<Integer> caseId = createNumber("caseId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath notAvailable = createBoolean("notAvailable");

    public final StringPath notes = createString("notes");
    
    public abstract ComparableExpressionBase<? extends Comparable<?>> getValue();
    
    public abstract Path<T> getValuePath(Class<? extends Object> cls);
    
    public abstract OrderSpecifier<? extends Comparable<?>> getValueOrderSpecifier(boolean ascending);

    public QCaseAttributeBase(PathMetadata<?> metadata, String schema, String table) {
		super(CaseAttribute.class, metadata, schema, table);
	}

    {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(caseId, ColumnMetadata.named("CASE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(attributeId, ColumnMetadata.named("ATTRIBUTE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(notAvailable, ColumnMetadata.named("NOT_AVAILABLE").withIndex(5).ofType(Types.BIT).notNull());
        addMetadata(notes, ColumnMetadata.named("NOTES").withIndex(6).ofType(Types.VARCHAR).withSize(2048));
    }
}
