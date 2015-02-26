package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCaseAttributeDates is a Querydsl query type for CaseAttributeDates
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QCaseAttributeDates extends com.mysema.query.sql.RelationalPathBase<CaseAttributeDates> {

    private static final long serialVersionUID = 1605860926;

    public static final QCaseAttributeDates caseAttributeDates = new QCaseAttributeDates("case_attribute_dates");

    public final BooleanPath active = createBoolean("active");

    public final StringPath attribute = createString("attribute");

    public final NumberPath<Integer> caseId = createNumber("caseId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modified = createDateTime("modified", java.sql.Timestamp.class);

    public final NumberPath<Integer> modifiedBy = createNumber("modifiedBy", Integer.class);

    public final BooleanPath notAvailable = createBoolean("notAvailable");

    public final StringPath notes = createString("notes");

    public final DatePath<java.sql.Date> value = createDate("value", java.sql.Date.class);

    public final com.mysema.query.sql.PrimaryKey<CaseAttributeDates> primary = createPrimaryKey(id);

    public QCaseAttributeDates(String variable) {
        super(CaseAttributeDates.class, forVariable(variable), "null", "case_attribute_dates");
        addMetadata();
    }

    public QCaseAttributeDates(String variable, String schema, String table) {
        super(CaseAttributeDates.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCaseAttributeDates(Path<? extends CaseAttributeDates> path) {
        super(path.getType(), path.getMetadata(), "null", "case_attribute_dates");
        addMetadata();
    }

    public QCaseAttributeDates(PathMetadata<?> metadata) {
        super(CaseAttributeDates.class, metadata, "null", "case_attribute_dates");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(active, ColumnMetadata.named("ACTIVE").withIndex(5).ofType(Types.BIT).notNull());
        addMetadata(attribute, ColumnMetadata.named("ATTRIBUTE").withIndex(6).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(caseId, ColumnMetadata.named("CASE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(modified, ColumnMetadata.named("MODIFIED").withIndex(3).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(modifiedBy, ColumnMetadata.named("MODIFIED_BY").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(notAvailable, ColumnMetadata.named("NOT_AVAILABLE").withIndex(8).ofType(Types.BIT).notNull());
        addMetadata(notes, ColumnMetadata.named("NOTES").withIndex(9).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(7).ofType(Types.DATE).withSize(10));
    }

}

