package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QViewAttributes is a Querydsl query type for ViewAttributes
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QViewAttributes extends com.mysema.query.sql.RelationalPathBase<ViewAttributes> {

    private static final long serialVersionUID = 1993272833;

    public static final QViewAttributes viewAttributes = new QViewAttributes("view_attributes");

    public final NumberPath<Integer> attributeId = createNumber("attributeId", Integer.class);

    public final StringPath options = createString("options");

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final NumberPath<Integer> viewId = createNumber("viewId", Integer.class);

    public final com.mysema.query.sql.PrimaryKey<ViewAttributes> primary = createPrimaryKey(attributeId, viewId);

    public QViewAttributes(String variable) {
        super(ViewAttributes.class, forVariable(variable), "null", "view_attributes");
        addMetadata();
    }

    public QViewAttributes(String variable, String schema, String table) {
        super(ViewAttributes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QViewAttributes(Path<? extends ViewAttributes> path) {
        super(path.getType(), path.getMetadata(), "null", "view_attributes");
        addMetadata();
    }

    public QViewAttributes(PathMetadata<?> metadata) {
        super(ViewAttributes.class, metadata, "null", "view_attributes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attributeId, ColumnMetadata.named("attribute_id").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(options, ColumnMetadata.named("options").withIndex(4).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(rank, ColumnMetadata.named("rank").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(viewId, ColumnMetadata.named("view_id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

