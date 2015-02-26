package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QStudyRolePermissions is a Querydsl query type for StudyRolePermissions
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QStudyRolePermissions extends com.mysema.query.sql.RelationalPathBase<StudyRolePermissions> {

    private static final long serialVersionUID = -1385521782;

    public static final QStudyRolePermissions studyRolePermissions = new QStudyRolePermissions("study_role_permissions");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath permission = createString("permission");

    public final StringPath resource = createString("resource");

    public final StringPath resourceType = createString("resourceType");

    public final NumberPath<Integer> studyRoleId = createNumber("studyRoleId", Integer.class);

    public final com.mysema.query.sql.PrimaryKey<StudyRolePermissions> primary = createPrimaryKey(id);

    public QStudyRolePermissions(String variable) {
        super(StudyRolePermissions.class, forVariable(variable), "null", "study_role_permissions");
        addMetadata();
    }

    public QStudyRolePermissions(String variable, String schema, String table) {
        super(StudyRolePermissions.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStudyRolePermissions(Path<? extends StudyRolePermissions> path) {
        super(path.getType(), path.getMetadata(), "null", "study_role_permissions");
        addMetadata();
    }

    public QStudyRolePermissions(PathMetadata<?> metadata) {
        super(StudyRolePermissions.class, metadata, "null", "study_role_permissions");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(permission, ColumnMetadata.named("PERMISSION").withIndex(5).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(resource, ColumnMetadata.named("RESOURCE").withIndex(4).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(resourceType, ColumnMetadata.named("RESOURCE_TYPE").withIndex(3).ofType(Types.VARCHAR).withSize(12).notNull());
        addMetadata(studyRoleId, ColumnMetadata.named("STUDY_ROLE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

