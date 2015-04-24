package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.View;

import com.mysema.query.types.path.*;
import com.mysema.query.types.PathMetadata;

import javax.annotation.Generated;

import com.mysema.query.types.Path;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;




/**
 * QViews is a Querydsl query type for Views
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QView extends com.mysema.query.sql.RelationalPathBase<View> {

    private static final long serialVersionUID = 260109481;

    public static final QView views = new QView("views");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final StringPath options = createString("options");

    public final com.mysema.query.sql.PrimaryKey<View> primary = createPrimaryKey(id);

    public QView(String variable) {
        super(View.class, forVariable(variable), "null", "views");
        addMetadata();
    }

    public QView(String variable, String schema, String table) {
        super(View.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QView(Path<? extends View> path) {
        super(path.getType(), path.getMetadata(), "null", "views");
        addMetadata();
    }

    public QView(PathMetadata<?> metadata) {
        super(View.class, metadata, "null", "views");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(4).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(studyId, ColumnMetadata.named("STUDY_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(options, ColumnMetadata.named("OPTIONS").withIndex(5).ofType(Types.VARCHAR).withSize(2048));
    }

}

