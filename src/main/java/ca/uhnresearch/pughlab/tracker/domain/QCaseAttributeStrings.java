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

    public final StringPath attribute = createString("attribute");

    public final NumberPath<Integer> caseId = createNumber("caseId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

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
        addMetadata(attribute, ColumnMetadata.named("ATTRIBUTE").withIndex(6).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(caseId, ColumnMetadata.named("CASE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(notAvailable, ColumnMetadata.named("NOT_AVAILABLE").withIndex(8).ofType(Types.BIT).notNull());
        addMetadata(notes, ColumnMetadata.named("NOTES").withIndex(9).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(value, ColumnMetadata.named("VALUE").withIndex(7).ofType(Types.VARCHAR).withSize(4096));
    }

}

