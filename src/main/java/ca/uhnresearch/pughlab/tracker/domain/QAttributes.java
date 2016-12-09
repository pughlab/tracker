package ca.uhnresearch.pughlab.tracker.domain;

import java.sql.Types;

import ca.uhnresearch.pughlab.tracker.dto.Attributes;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.PathMetadataFactory;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public class QAttributes extends com.mysema.query.sql.RelationalPathBase<Attributes> {

    private static final long serialVersionUID = 1988295676;

    public static final QAttributes attributes = new QAttributes("attributes");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath label = createString("label");

    public final StringPath name = createString("name");

    public final StringPath options = createString("options");

    public final NumberPath<Integer> rank = createNumber("rank", Integer.class);

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final StringPath type = createString("type");

    public final com.mysema.query.sql.PrimaryKey<Attributes> primary = createPrimaryKey(id);

    public QAttributes(String variable) {
        super(Attributes.class, PathMetadataFactory.forVariable(variable), "null", "ATTRIBUTES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(description, ColumnMetadata.named("DESCRIPTION").withIndex(4).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(label, ColumnMetadata.named("LABEL").withIndex(5).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(48).notNull());
        addMetadata(options, ColumnMetadata.named("OPTIONS").withIndex(8).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(rank, ColumnMetadata.named("RANK").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(studyId, ColumnMetadata.named("STUDY_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.VARCHAR).withSize(24).notNull());
    }

}

