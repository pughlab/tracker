package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import ca.uhnresearch.pughlab.tracker.dto.User;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public class QUser extends com.mysema.query.sql.RelationalPathBase<User> {
	
	private static final long serialVersionUID = 7469280514362382681L;

	public static final QUser users = new QUser("users");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath username = createString("username");

    public final com.mysema.query.sql.PrimaryKey<User> primary = createPrimaryKey(id);

    public QUser(String variable) {
        super(User.class, forVariable(variable), "null", "users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(username, ColumnMetadata.named("USERNAME").withIndex(2).ofType(Types.VARCHAR).withSize(24).notNull());
    }

}
