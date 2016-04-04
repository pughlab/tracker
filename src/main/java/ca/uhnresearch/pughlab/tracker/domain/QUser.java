package ca.uhnresearch.pughlab.tracker.domain;

import static com.mysema.query.types.PathMetadataFactory.*;
import ca.uhnresearch.pughlab.tracker.dto.User;

import com.mysema.query.types.path.*;
import com.mysema.query.sql.ColumnMetadata;

import java.sql.Types;

public class QUser extends com.mysema.query.sql.RelationalPathBase<User> {

	private static final long serialVersionUID = -8721922821765112884L;

	public static final QUser users = new QUser("users");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath username = createString("username");

    public final BooleanPath administrator = createBoolean("administrator");

    public final StringPath email = createString("email");

    public final StringPath displayName = createString("displayName");

    public final StringPath givenName = createString("givenName");

    public final StringPath familyName = createString("familyName");

    public final com.mysema.query.sql.PrimaryKey<User> primary = createPrimaryKey(id);

    public QUser(String variable) {
        super(User.class, forVariable(variable), "null", "USERS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(username, ColumnMetadata.named("USERNAME").withIndex(2).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(administrator, ColumnMetadata.named("ADMINISTRATOR").withIndex(3).ofType(Types.BIT));
        addMetadata(email, ColumnMetadata.named("EMAIL").withIndex(4).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(displayName, ColumnMetadata.named("DISPLAY_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(givenName, ColumnMetadata.named("GIVEN_NAME").withIndex(6).ofType(Types.VARCHAR).withSize(64).notNull());
        addMetadata(familyName, ColumnMetadata.named("FAMILY_NAME").withIndex(7).ofType(Types.VARCHAR).withSize(64).notNull());
    }
}

