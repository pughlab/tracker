package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCaseAttributeBooleans is a Querydsl query type for CaseAttributeBooleans
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QCaseAttributeBooleans extends com.mysema.query.sql.RelationalPathBase<CaseAttributeBooleans> {

    private static final long serialVersionUID = -1225368846;

    public static final QCaseAttributeBooleans caseAttributeBooleans = new QCaseAttributeBooleans("case_attribute_booleans");

    public final BooleanPath active = createBoolean("active");

    public final StringPath attribute = createString("attribute");

    public final NumberPath<Integer> caseId = createNumber("caseId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modified = createDateTime("modified", java.sql.Timestamp.class);

    public final NumberPath<Integer> modifiedBy = createNumber("modifiedBy", Integer.class);

    public final BooleanPath notAvailable = createBoolean("notAvailable");

    public final StringPath notes = createString("notes");

    public final BooleanPath value = createBoolean("value");

    public final com.mysema.query.sql.PrimaryKey<CaseAttributeBooleans> primary = createPrimaryKey(id);

    public QCaseAttributeBooleans(String variable) {
        super(CaseAttributeBooleans.class, forVariable(variable), "null", "case_attribute_booleans");
        addMetadata();
    }

    public QCaseAttributeBooleans(String variable, String schema, String table) {
        super(CaseAttributeBooleans.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCaseAttributeBooleans(Path<? extends CaseAttributeBooleans> path) {
        super(path.getType(), path.getMetadata(), "null", "case_attribute_booleans");
        addMetadata();
    }

    public QCaseAttributeBooleans(PathMetadata<?> metadata) {
        super(CaseAttributeBooleans.class, metadata, "null", "case_attribute_booleans");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(active, ColumnMetadata.named("active").withIndex(5).ofType(Types.BIT).notNull());
        addMetadata(attribute, ColumnMetadata.named("attribute").withIndex(6).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(caseId, ColumnMetadata.named("case_id").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modified, ColumnMetadata.named("modified").withIndex(3).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(modifiedBy, ColumnMetadata.named("modified_by").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(notAvailable, ColumnMetadata.named("not_available").withIndex(8).ofType(Types.BIT).notNull());
        addMetadata(notes, ColumnMetadata.named("notes").withIndex(9).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(value, ColumnMetadata.named("value").withIndex(7).ofType(Types.BIT));
    }

}

