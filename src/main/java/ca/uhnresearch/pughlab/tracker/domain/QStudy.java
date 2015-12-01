package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.Study;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

public class QStudy extends com.mysema.query.sql.RelationalPathBase<Study> {

    private static final long serialVersionUID = -1476278366;

    public static final QStudy studies = new QStudy("studies");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath options = createString("options");

    public final StringPath about = createString("about");

    public final com.mysema.query.sql.PrimaryKey<Study> primary = createPrimaryKey(id);

    public QStudy(String variable) {
        super(Study.class, forVariable(variable), "null", "STUDIES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(3).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(about, ColumnMetadata.named("ABOUT").withIndex(4).ofType(Types.LONGVARCHAR).withSize(65535));
        addMetadata(options, ColumnMetadata.named("OPTIONS").withIndex(5).ofType(Types.VARCHAR).withSize(2048));
    }

}

