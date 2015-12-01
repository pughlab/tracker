package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import ca.uhnresearch.pughlab.tracker.dto.UserRole;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public class QUserRole extends com.mysema.query.sql.RelationalPathBase<UserRole>{

	private static final long serialVersionUID = -8545581443700216871L;

	public static final QUserRole userRoles = new QUserRole("user_roles");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);
    
    public final StringPath username = createString("username");

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public QUserRole(String variable) {
        super(UserRole.class, forVariable(variable), "null", "USER_ROLES");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(username, ColumnMetadata.named("USERNAME").withIndex(2).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(roleId, ColumnMetadata.named("ROLE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
    }

}
