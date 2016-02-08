package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.View;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

public class QView extends com.mysema.query.sql.RelationalPathBase<View> {

    private static final long serialVersionUID = 260109481;

    public static final QView views = new QView("views");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final StringPath options = createString("options");

    public final StringPath body = createString("body");

    public final com.mysema.query.sql.PrimaryKey<View> primary = createPrimaryKey(id);

    public QView(String variable) {
        super(View.class, forVariable(variable), "null", "VIEWS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(studyId, ColumnMetadata.named("STUDY_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(4).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(body, ColumnMetadata.named("BODY").withIndex(5).ofType(Types.CLOB));
        addMetadata(options, ColumnMetadata.named("OPTIONS").withIndex(6).ofType(Types.VARCHAR).withSize(2048));
    }

}

