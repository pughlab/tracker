package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QStudyRoleUsers is a Querydsl query type for StudyRoleUsers
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QStudyRoleUsers extends com.mysema.query.sql.RelationalPathBase<StudyRoleUsers> {

    private static final long serialVersionUID = -241503346;

    public static final QStudyRoleUsers studyRoleUsers = new QStudyRoleUsers("study_role_users");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> studyRoleId = createNumber("studyRoleId", Integer.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.mysema.query.sql.PrimaryKey<StudyRoleUsers> primary = createPrimaryKey(id);

    public QStudyRoleUsers(String variable) {
        super(StudyRoleUsers.class, forVariable(variable), "null", "study_role_users");
        addMetadata();
    }

    public QStudyRoleUsers(String variable, String schema, String table) {
        super(StudyRoleUsers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStudyRoleUsers(Path<? extends StudyRoleUsers> path) {
        super(path.getType(), path.getMetadata(), "null", "study_role_users");
        addMetadata();
    }

    public QStudyRoleUsers(PathMetadata<?> metadata) {
        super(StudyRoleUsers.class, metadata, "null", "study_role_users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(studyRoleId, ColumnMetadata.named("STUDY_ROLE_ID").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

