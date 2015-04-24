package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.Study;

import com.mysema.query.types.path.*;
import com.mysema.query.types.PathMetadata;

import javax.annotation.Generated;

import com.mysema.query.types.Path;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;




/**
 * QStudies is a Querydsl query type for Studies
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QStudy extends com.mysema.query.sql.RelationalPathBase<Study> {

    private static final long serialVersionUID = -1476278366;

    public static final QStudy studies = new QStudy("studies");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.mysema.query.sql.PrimaryKey<Study> primary = createPrimaryKey(id);

    public QStudy(String variable) {
        super(Study.class, forVariable(variable), "null", "studies");
        addMetadata();
    }

    public QStudy(String variable, String schema, String table) {
        super(Study.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStudy(Path<? extends Study> path) {
        super(path.getType(), path.getMetadata(), "null", "studies");
        addMetadata();
    }

    public QStudy(PathMetadata<?> metadata) {
        super(Study.class, metadata, "null", "studies");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(3).ofType(Types.VARCHAR).withSize(2048));
    }

}

