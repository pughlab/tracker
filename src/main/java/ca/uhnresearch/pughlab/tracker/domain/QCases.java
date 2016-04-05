package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.Cases;

import com.mysema.query.types.path.*;

import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

public class QCases extends com.mysema.query.sql.RelationalPathBase<Cases> {

    private static final long serialVersionUID = 242337150;

    public static final QCases cases = new QCases("cases");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath guid = createString("guid");

    public final StringPath state = createString("state");

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final NumberPath<Integer> order = createNumber("order", Integer.class);

    public final com.mysema.query.sql.PrimaryKey<Cases> primary = createPrimaryKey(id);

    public QCases(String variable) {
        super(Cases.class, forVariable(variable), "null", "CASES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(guid, ColumnMetadata.named("GUID").withIndex(2).ofType(Types.VARCHAR).withSize(36));
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(studyId, ColumnMetadata.named("STUDY_ID").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(order, ColumnMetadata.named("ORDER").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

