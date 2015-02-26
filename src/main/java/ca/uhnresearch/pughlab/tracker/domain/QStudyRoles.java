package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QStudyRoles is a Querydsl query type for StudyRoles
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QStudyRoles extends com.mysema.query.sql.RelationalPathBase<StudyRoles> {

    private static final long serialVersionUID = 1096816921;

    public static final QStudyRoles studyRoles = new QStudyRoles("study_roles");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final com.mysema.query.sql.PrimaryKey<StudyRoles> primary = createPrimaryKey(id);

    public QStudyRoles(String variable) {
        super(StudyRoles.class, forVariable(variable), "null", "study_roles");
        addMetadata();
    }

    public QStudyRoles(String variable, String schema, String table) {
        super(StudyRoles.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStudyRoles(Path<? extends StudyRoles> path) {
        super(path.getType(), path.getMetadata(), "null", "study_roles");
        addMetadata();
    }

    public QStudyRoles(PathMetadata<?> metadata) {
        super(StudyRoles.class, metadata, "null", "study_roles");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(description, ColumnMetadata.named("description").withIndex(4).ofType(Types.VARCHAR).withSize(2048));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(studyId, ColumnMetadata.named("study_id").withIndex(2).ofType(Types.INTEGER).withSize(10));
    }

}

