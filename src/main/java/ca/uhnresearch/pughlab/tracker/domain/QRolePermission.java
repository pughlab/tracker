package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

import ca.uhnresearch.pughlab.tracker.dto.RolePermission;

public class QRolePermission extends com.mysema.query.sql.RelationalPathBase<RolePermission>{

	private static final long serialVersionUID = 8202249556368568993L;

	public static final QRolePermission rolePermissions = new QRolePermission("rolePermissions");

	public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public final StringPath permission = createString("permission");

    public QRolePermission(String variable) {
        super(RolePermission.class, forVariable(variable), "null", "ROLE_PERMISSIONS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(roleId, ColumnMetadata.named("ROLE_ID").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(permission, ColumnMetadata.named("PERMISSION").ofType(Types.VARCHAR).withSize(48).notNull());
    }

}
