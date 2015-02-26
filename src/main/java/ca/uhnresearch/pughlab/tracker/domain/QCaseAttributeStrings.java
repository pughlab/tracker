package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCaseAttributeStrings is a Querydsl query type for CaseAttributeStrings
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QCaseAttributeStrings extends com.mysema.query.sql.RelationalPathBase<CaseAttributeStrings> {

    private static final long serialVersionUID = -1985998853;

    public static final QCaseAttributeStrings caseAttributeStrings = new QCaseAttributeStrings("case_attribute_strings");

    public final BooleanPath active = createBoolean("active");

    public final StringPath attribute = createString("attribute");

    public final NumberPath<Integer> caseId = createNumber("caseId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.sql.Timestamp> modified = createDateTime("modified", java.sql.Timestamp.class);

    public final NumberPath<Integer> modifiedBy = createNumber("modifiedBy", Integer.class);

    public final BooleanPath notAvailable = createBoolean("notAvailable");

    public final StringPath notes = createString("notes");

    public final StringPath value = createString("value");

    public final com.mysema.query.sql.PrimaryKey<CaseAttributeStrings> primary = createPrimaryKey(id);

    public QCaseAttributeStrings(String variable) {
        super(CaseAttributeStrings.class, forVariable(variable), "null", "case_attribute_strings");
        addMetadata();
    }

    public QCaseAttributeStrings(String variable, String schema, String table) {
        super(CaseAttributeStrings.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCaseAttributeStrings(Path<? extends CaseAttributeStrings> path) {
        super(path.getType(), path.getMetadata(), "null", "case_attribute_strings");
        addMetadata();
    }

    public QCaseAttributeStrings(PathMetadata<?> metadata) {
        super(CaseAttributeStrings.class, metadata, "null", "case_attribute_strings");
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
        addMetadata(value, ColumnMetadata.named("value").withIndex(7).ofType(Types.VARCHAR).withSize(4096));
    }

}

