package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import ca.uhnresearch.pughlab.tracker.dto.Role;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public class QRole extends com.mysema.query.sql.RelationalPathBase<Role> {
	
	private static final long serialVersionUID = 4817944294016671308L;

	public static final QRole roles = new QRole("roles");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> studyId = createNumber("studyId", Integer.class);

    public final StringPath name = createString("name");

    public final com.mysema.query.sql.PrimaryKey<Role> primary = createPrimaryKey(id);

    public QRole(String variable) {
        super(Role.class, forVariable(variable), "null", "ROLES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(studyId, ColumnMetadata.named("STUDY_ID").withIndex(1).ofType(Types.INTEGER).withSize(10));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(24).notNull());
    }
}
