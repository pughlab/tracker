package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCases is a Querydsl query type for Cases
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QCases extends com.mysema.query.sql.RelationalPathBase<Cases> {

    private static final long serialVersionUID = 242337150;

    public static final QCases cases = new QCases("cases");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath state = createString("state");

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final com.mysema.query.sql.PrimaryKey<Cases> primary = createPrimaryKey(id);

    public QCases(String variable) {
        super(Cases.class, forVariable(variable), "null", "cases");
        addMetadata();
    }

    public QCases(String variable, String schema, String table) {
        super(Cases.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCases(Path<? extends Cases> path) {
        super(path.getType(), path.getMetadata(), "null", "cases");
        addMetadata();
    }

    public QCases(PathMetadata<?> metadata) {
        super(Cases.class, metadata, "null", "cases");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(3).ofType(Types.VARCHAR).withSize(255));
        addMetadata(studyId, ColumnMetadata.named("STUDY_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

