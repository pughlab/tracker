package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.ViewAttributes;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

public class QViewAttributes extends com.mysema.query.sql.RelationalPathBase<ViewAttributes> {

    private static final long serialVersionUID = 1993272833;

    public static final QViewAttributes viewAttributes = new QViewAttributes("view_attributes");

    public final NumberPath<Integer> attributeId = createNumber("attributeId", Integer.class);

    public final StringPath options = createString("options");

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final NumberPath<Integer> viewId = createNumber("viewId", Integer.class);

    public final com.mysema.query.sql.PrimaryKey<ViewAttributes> primary = createPrimaryKey(attributeId, viewId);

    public QViewAttributes(String variable) {
        super(ViewAttributes.class, forVariable(variable), "null", "VIEW_ATTRIBUTES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attributeId, ColumnMetadata.named("ATTRIBUTE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(options, ColumnMetadata.named("OPTIONS").withIndex(4).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(rank, ColumnMetadata.named("RANK").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(viewId, ColumnMetadata.named("VIEW_ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

