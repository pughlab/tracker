package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

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
public class QStudies extends com.mysema.query.sql.RelationalPathBase<Studies> {

    private static final long serialVersionUID = -1476278366;

    public static final QStudies studies = new QStudies("studies");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> identifierAttributeId = createNumber("identifierAttributeId", Integer.class);

    public final StringPath name = createString("name");

    public final com.mysema.query.sql.PrimaryKey<Studies> primary = createPrimaryKey(id);

    public QStudies(String variable) {
        super(Studies.class, forVariable(variable), "null", "studies");
        addMetadata();
    }

    public QStudies(String variable, String schema, String table) {
        super(Studies.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStudies(Path<? extends Studies> path) {
        super(path.getType(), path.getMetadata(), "null", "studies");
        addMetadata();
    }

    public QStudies(PathMetadata<?> metadata) {
        super(Studies.class, metadata, "null", "studies");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(description, ColumnMetadata.named("description").withIndex(3).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(identifierAttributeId, ColumnMetadata.named("identifier_attribute_id").withIndex(4).ofType(Types.INTEGER).withSize(10));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(24).notNull());
    }

}

